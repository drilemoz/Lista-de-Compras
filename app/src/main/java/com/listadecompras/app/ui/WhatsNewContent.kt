package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 46
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Supermercado e Shopping agora têm fluxos próprios."

    val changes = listOf(
        Change("Dois tipos de lista", "Ao criar, escolha Supermercado para manter medidas e combos, ou Shopping para links e parcelas."),
        Change("Pagamento parcelado", "Calcule total, entrada, frete, taxas, percentual de acréscimo e próxima data de vencimento."),
        Change("Aviso acima de 50%", "O aplicativo destaca acréscimos totais altos, sem chamá-los de taxa de juros mensal."),
        Change("Comparar lojas", "Guarde uma segunda oferta com link, preço, entrada, frete e valor parcelado para comparar o custo final."),
        Change("Parcelas pagas", "Registre quantas prestações já foram pagas e confira quantas restam."),
        Change("Dados preservados", "Listas antigas continuam no modo Supermercado; backups anteriores seguem compatíveis.")
    )

    data class Change(val title: String, val description: String)
}
