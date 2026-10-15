package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 47
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Correção da validação dos campos de Shopping e melhorias do modo de compras."

    val changes = listOf(
        Change("Build 8 corrigido", "Validação dos campos Loja e Variante no Shopping foi corrigida para aceitar o texto editável do Android sem erro de compilação."),
        Change("Dois tipos de lista", "Ao criar, escolha Supermercado para manter medidas e combos, ou Shopping para links e parcelas."),
        Change("Pagamento parcelado", "Calcule total, entrada, frete, taxas, percentual de acréscimo e próxima data de vencimento."),
        Change("Aviso acima de 50%", "O aplicativo destaca acréscimos totais altos, sem chamá-los de taxa de juros mensal."),
        Change("Comparar lojas", "Guarde uma segunda oferta com link, preço, entrada, frete e valor parcelado para comparar o custo final."),
        Change("Parcelas pagas", "Registre quantas prestações já foram pagas e confira quantas restam."),
        Change("Dados preservados", "Listas antigas continuam no modo Supermercado; backups anteriores seguem compatíveis.")
    )

    data class Change(val title: String, val description: String)
}
