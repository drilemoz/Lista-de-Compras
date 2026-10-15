package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 49
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Foto corrigida e links organizados com ícones."

    val changes = listOf(
        Change("Galeria e câmera", "Corrigida a falha que mostrava 'Imagem indisponível' ao importar uma foto válida."),
        Change("Links lado a lado", "Abrir, Copiar e Compartilhar agora aparecem na mesma linha com ícones."),
        Change("Segunda oferta", "Os atalhos da oferta alternativa usam o mesmo padrão, quando o link existir."),
        Change("Dados preservados", "Imagens já salvas, listas, parcelamento e backup ZIP schema 9 continuam compatíveis.")
    )

    data class Change(val title: String, val description: String)
}
