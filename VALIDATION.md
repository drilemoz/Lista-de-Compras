# Validação 1.0.41+45

- Build 6 (logs): falha na validação de fonte antes de Gradle por expectativa obsoleta de `shop="supermarket"`.
- Corrigido `scripts/validate_source.py` para reconhecer `MarketOverpassClient.kt` e conferir tipos de estabelecimentos, feira, nome/marca, raio e tags.
- Refatorado `NearbyMarketsActivity.kt` de 577 para 469 linhas; o módulo `MarketOverpassClient.kt` tem 120 linhas.
- Corrigido guard `scripts/validate_lint_build25.py`: o novo card tem emoji `TextView` e um `ImageButton`, sem `android:tint`.
- Aprovados localmente: 15 validadores Python incluindo assinatura (casos com/sem Secrets); 2 testes Kotlin/JVM independentes (12 testes de entrada e 13 verificações finais); parser Kotlin PSI (60 arquivos); 58 XML; 2 JSON; sintaxe JavaScript embutida no mapa.
- `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:lintDebug`, assinatura Release e execução no Android real não rodaram neste ambiente sem Android SDK/Gradle; requer CI.
