# Validação 1.0.45+49

- Executar `scripts/validate_photo_link_fix.py` para proteger contra interpretação incorreta do `decodeStream` em modo dimensões e contra perda dos três ícones alinhados.
- Executar todos os outros validadores Python (`scripts/validate_*.py` e teste de assinatura).
- Confirmação necessária no CI: `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:lintDebug`, assinatura Release.
- Teste Android manual: galeria retorna foto válida sem alerta, imagem é exibida nos detalhes e miniatura; câmera salva; rotacionar aparelho; reiniciar app; backup ZIP com foto restaura; nenhum link ou link alternativo, abrir/copiar/compartilhar com ações lado a lado.
- Schema de backup 9 permanece inalterado; os arquivos antigos continuam válidos.
