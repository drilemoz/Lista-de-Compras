# Validação 1.0.43+47

- Origem: logs Build 8, etapa `:app:compileDebugKotlin`: três `Unresolved reference 'orEmpty'` em `ShoppingFieldsForm.kt:143`.
- Corrigida a leitura de `Editable` dos campos de loja/variante antes da chamada de `orEmpty()`.
- Novo guard de regressão no `scripts/validate_shopping_mode.py`, além dos demais validadores existentes.
- Testar no CI: `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:lintDebug` e eventual Release assinada.
- Teste manual: campo Loja/Variante/Outra loja até 120 caracteres, link, parcelamento, comparação e restauração de backup.
- Compilação Android completa não realizada localmente se faltarem Gradle/Android SDK; não considerar APK validado sem execução no CI.
