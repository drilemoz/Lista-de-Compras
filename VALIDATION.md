# Validação 1.0.38+42

- Conferir `scripts/validate_price_split_and_ui.py` para o contrato de calculadora, layouts e serializadores.
- Conferir `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:lintDebug` e assinatura Release no GitHub Actions.
- Testes JUnit `PriceSplitHelperTest` incluem ofertas com fração de centavo, quantidade parcial, preços por kg e regressão de itens sem oferta.
- UI manual: teclado e IME, cabeçalho e ação salvar fixos, 20 emojis em grade, alteração de categoria padrão e personalizada, recuperação de combo na edição e restauração JSON antigo/novo.
- Sem build Gradle Android completo local por ausência de wrapper/SDK neste ambiente; CI precisa confirmar.
