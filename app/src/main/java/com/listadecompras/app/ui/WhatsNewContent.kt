package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 44
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Listas mais elegantes e mercados próximos com busca e mapa melhores."

    val changes = listOf(
        Change("Listas mais modernas", "Os cartões da tela inicial ficaram mais informativos, com estado da compra, mercado vinculado e visual mais atraente."),
        Change("Sugestões alinhadas", "A criação de listas ganhou sugestões rápidas em grade, corrigindo o desalinhamento e melhorando a área de toque."),
        Change("Busca ampliada", "Mercados próximos agora pesquisa supermercados, mercearias, hortifrutis, conveniências, atacados e feiras/marketplaces."),
        Change("Mapa corrigido", "O mapa OpenStreetMap/Leaflet recebeu ajustes de carregamento, redimensionamento e marcadores mais confiáveis."),
        Change("Resultados mais claros", "A tela mostra contagem de mercados, tipo do estabelecimento, estado vazio contextual e ações mais objetivas."),
        Change("Dados preservados", "Favoritos, cache recente e listas associadas aos mercados continuam compatíveis com a base atual.")
    )

    data class Change(val title: String, val description: String)
}
