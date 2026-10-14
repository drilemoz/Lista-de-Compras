package com.listamercado.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 39
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "O aplicativo agora se chama Lista de Compras."

    val changes = listOf(
        Change("Novo nome", "O aplicativo e a tela Sobre agora exibem Lista de Compras."),
        Change("Arquivos e releases", "Novos backups, arquivos CSV e APKs publicados seguem o novo nome."),
        Change("Backup compatível", "Arquivos exportados em versões anteriores continuam aceitos."),
        Change("Dados preservados", "Listas, preços, histórico, configurações e assinatura permanecem compatíveis.")
    )

    data class Change(val title: String, val description: String)
}
