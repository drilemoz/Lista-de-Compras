package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 41
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Correção da geração de APKs e assinatura no GitHub Actions."

    val changes = listOf(
        Change("Correção na geração do APK", "O workflow passa a disponibilizar o APK Debug mesmo quando a assinatura de produção não está configurada."),
        Change("Release mais segura", "Com os quatro Secrets da mesma assinatura, a compilação gera e verifica o APK Release assinado."),
        Change("Categorias e listas", "Continuam disponíveis as categorias com ícones, o novo editor de listas e os backups compatíveis.")
    )

    data class Change(val title: String, val description: String)
}
