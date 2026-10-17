package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 50
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Lista de compras mais compacta e informações mais claras."

    val changes = listOf(
        Change("Orçamento mais compacto", "O cartão de orçamento ficou mais baixo. Toque em qualquer ponto dele para definir ou alterar o valor."),
        Change("Filtros diretos", "Todos, Pendentes e Comprados continuam funcionando com um único toque, com botões de aparência mais compacta."),
        Change("Produtos com menos espaço", "Reduzidos os espaços internos dos cartões, sem esconder a foto, o preço e as opções."),
        Change("À vista sem redundância", "Produtos sem parcelamento mostram somente o preço à vista, sem o texto 'sem parcelas'."),
        Change("Link junto da categoria", "Quando o produto tiver anúncio salvo, a indicação de link aparece ao lado da categoria."),
        Change("Dados preservados", "Listas, fotos, parcelamento e backup ZIP schema 9 continuam compatíveis.")
    )

    data class Change(val title: String, val description: String)
}
