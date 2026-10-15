package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 45
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Correção de validação da busca ampliada de mercados no GitHub Actions."

    val changes = listOf(
        Change("Build 6 corrigido", "O validador do CI agora aceita a busca Overpass ampliada sem confundi-la com a consulta antiga."),
        Change("Cobertura preservada", "A validação continua exigindo supermercados, mercearias, conveniências, atacados e hortifrutis."),
        Change("Funcionalidades mantidas", "As telas de listas, categorias, compras, mapa, backup e assinatura não tiveram seu comportamento alterado.")
    )

    data class Change(val title: String, val description: String)
}
