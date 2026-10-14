#!/usr/bin/env bash
# Resolve one COMPLETE and coherent signing set. Never combine values from different sets.
set -euo pipefail

: "${GITHUB_OUTPUT:?GITHUB_OUTPUT is required}"
: "${GITHUB_ENV:?GITHUB_ENV is required}"
: "${RUNNER_TEMP:?RUNNER_TEMP is required}"

selected=""
keystore_base64=""
store_password=""
key_alias=""
key_password=""
partial="false"

# Project-specific signing keys take precedence over generic Android signing keys.
for source in LISTA_COMPRAS LISTA_MERCADO ANDROID KEYSTORE SIGNING; do
    case "$source" in
        LISTA_COMPRAS)
            ks="${KS_LISTA_COMPRAS:-}"; sp="${STORE_LISTA_COMPRAS:-}"
            alias="${ALIAS_LISTA_COMPRAS:-}"; kp="${KEY_LISTA_COMPRAS:-}" ;;
        LISTA_MERCADO)
            ks="${KS_LISTA_MERCADO:-}"; sp="${STORE_LISTA_MERCADO:-}"
            alias="${ALIAS_LISTA_MERCADO:-}"; kp="${KEY_LISTA_MERCADO:-}" ;;
        ANDROID)
            ks="${KS_ANDROID:-}"; sp="${STORE_ANDROID:-}"
            alias="${ALIAS_ANDROID:-}"; kp="${KEY_ANDROID:-}" ;;
        KEYSTORE)
            ks="${KS_GENERIC:-}"; sp="${STORE_GENERIC:-}"
            alias="${ALIAS_GENERIC:-}"; kp="${KEY_GENERIC:-}" ;;
        SIGNING)
            ks="${KS_SIGNING:-}"; sp="${STORE_SIGNING:-}"
            alias="${ALIAS_SIGNING:-}"; kp="${KEY_SIGNING:-}" ;;
    esac
    if [[ -n "$ks" && -n "$sp" && -n "$alias" && -n "$kp" ]]; then
        selected="$source"
        keystore_base64="$ks"
        store_password="$sp"
        key_alias="$alias"
        key_password="$kp"
        break
    fi
    if [[ -n "$ks" || -n "$sp" || -n "$alias" || -n "$kp" ]]; then
        partial="true"
    fi
done

if [[ -z "$selected" ]]; then
    echo "enabled=false" >> "$GITHUB_OUTPUT"
    if [[ "$partial" == "true" ]]; then
        echo "reason=incomplete" >> "$GITHUB_OUTPUT"
        echo "::warning::Nenhum conjunto completo de 4 Secrets de assinatura foi encontrado. APK Debug disponível; Release assinada ignorada."
    else
        echo "reason=missing" >> "$GITHUB_OUTPUT"
        echo "::warning::Secrets de assinatura não configurados. APK Debug disponível; Release assinada ignorada."
    fi
    exit 0
fi

# GitHub environment files accept one line per value; block line breaks in credentials.
for credential in "$store_password" "$key_alias" "$key_password"; do
    if [[ "$credential" == *$'\n'* || "$credential" == *$'\r'* ]]; then
        echo "::error::Credencial de assinatura contém quebra de linha."
        exit 1
    fi
done

umask 077
keystore_file="$RUNNER_TEMP/lista-de-compras-release.jks"
trap 'rm -f "$keystore_file"' ERR
if ! printf '%s' "$keystore_base64" | tr -d '\r\n ' | base64 --decode > "$keystore_file"; then
    rm -f "$keystore_file"
    echo "::error::Keystore Base64 inválido no conjunto $selected."
    exit 1
fi
if [[ ! -s "$keystore_file" ]]; then
    rm -f "$keystore_file"
    echo "::error::Keystore vazio no conjunto $selected."
    exit 1
fi
if ! keytool -list -keystore "$keystore_file" -storepass "$store_password" -alias "$key_alias" >/dev/null 2>&1; then
    rm -f "$keystore_file"
    echo "::error::Não foi possível abrir a keystore e localizar o alias em $selected; confira arquivo, senha e alias."
    exit 1
fi

# Only a boolean status travels in step outputs. Secrets remain scoped to this job.
{
    printf 'LISTA_COMPRAS_KEYSTORE_PATH=%s\n' "$keystore_file"
    printf 'LISTA_COMPRAS_KEYSTORE_PASSWORD=%s\n' "$store_password"
    printf 'LISTA_COMPRAS_KEY_ALIAS=%s\n' "$key_alias"
    printf 'LISTA_COMPRAS_KEY_PASSWORD=%s\n' "$key_password"
} >> "$GITHUB_ENV"
{
    echo "enabled=true"
    echo "source=$selected"
} >> "$GITHUB_OUTPUT"
trap - ERR
echo "Chave de assinatura localizada e keystore validada: $selected."
