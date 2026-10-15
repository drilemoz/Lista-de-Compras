# Validação 1.0.40+44

- Executados localmente com sucesso: `scripts/validate_ui_restyle.py`, `scripts/validate_purchase_catalog.py`, `scripts/validate_branding.py` e `scripts/validate_final.py`.
- Conferir no GitHub Actions: `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:lintDebug` e assinatura Release, como nas versões anteriores.
- UI manual recomendada: cartões da home, criação/renomeação de listas com sugestões 2x2, busca de mercados por 5/10/20/30/50 km, favoritos, criação de lista a partir do mercado, cache offline e reabertura da tela.
- Validar em aparelho o mapa Leaflet/OpenStreetMap, especialmente o recálculo de layout após abrir a tela e após mudar o raio.
- Sem build Gradle Android completo local por ausência de wrapper/SDK neste ambiente; CI precisa confirmar.
