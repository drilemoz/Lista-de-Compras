package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 42
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Cadastro de produtos renovado, ofertas mais claras e categorias personalizadas."

    val changes = listOf(
        Change("Editor renovado", "Campos compactos, cabeçalho e botão Salvar fixos, e opções extras recolhíveis."),
        Change("Divisão de preço", "Informe o valor do combo e quantas unidades ele contém. O preço unitário é calculado sem alterar o total original."),
        Change("Ícones em grade", "Escolha símbolos em uma grade e edite o ícone de uma categoria existente tocando no emoji."),
        Change("Backup atualizado", "Ofertas com valor original e quantidade são preservadas no backup JSON schema 7 e nos modelos.")
    )

    data class Change(val title: String, val description: String)
}
