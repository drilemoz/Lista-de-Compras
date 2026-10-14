package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 40
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Nova identidade e mais liberdade para organizar suas compras."

    val changes = listOf(
        Change("Identificador atualizado", "Novo identificador com.listadecompras.app. Para migrar dados de versões anteriores, exporte e importe um backup."),
        Change("Categorias com ícones", "Cadastre categorias personalizadas com ícones. Elas permanecem no SQLite local para todas as listas."),
        Change("Criar listas", "Nova tela com opções claras para começar do zero ou usar um modelo, além de sugestões de nomes."),
        Change("Backups", "O schema 6 exporta as categorias. Backups antigos continuam compatíveis.")
    )

    data class Change(val title: String, val description: String)
}
