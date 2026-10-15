package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 43
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Cadastro de produtos renovado, ofertas mais claras e categorias personalizadas."

    val changes = listOf(
        Change("Editor renovado", "Campos compactos, cabeçalho e botão Salvar fixos, e opções extras recolhíveis."),
        Change("Divisão de preço", "Informe o valor do combo e quantas unidades ele contém. O preço unitário é calculado sem alterar o total original."),
        Change("Campo de nome ajustado", "O campo Nome do produto ganhou mais espaçamento interno para separar melhor o rótulo e o texto digitado."),
        Change("Mais ícones", "A biblioteca foi ampliada com opções para tecnologia, carros, bicicleta, ferramentas, roupas, games e outros temas."),
        Change("Seletor rolável", "A grade de ícones continua em blocos, mas agora pode rolar para acomodar as novas opções com conforto."),
        Change("Backup atualizado", "Ofertas com valor original e quantidade são preservadas no backup JSON schema 7 e nos modelos.")
    )

    data class Change(val title: String, val description: String)
}
