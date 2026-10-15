package com.listadecompras.app.ui

object WhatsNewContent {
    const val CONTENT_VERSION_CODE = 48
    const val TITLE = "O que mudou nesta atualização"
    const val SUBTITLE = "Detalhes de cada produto, fotos e links ao alcance de um toque."

    val changes = listOf(
        Change("Detalhes sem editar", "Toque em qualquer produto para consultar preços, quantidade, informações e observações sem abrir o teclado."),
        Change("Abrir, copiar e compartilhar", "No Shopping, os links dos anúncios ganharam botões diretos, com suporte a uma segunda oferta."),
        Change("Fotos do produto", "Escolha uma imagem da galeria ou fotografe com a câmera. A foto aparece no cadastro, no card e nos detalhes."),
        Change("Parcelamento nos detalhes", "Veja parcelas pagas, total financiado, próxima data e aviso de acréscimo total superior a 50%."),
        Change("Backup com imagens", "Novo ZIP inclui dados JSON schema 9 e fotos locais; backups JSON anteriores continuam importáveis."),
        Change("Supermercado preservado", "Listas existentes, itens, categorias, compras e cálculos continuam compatíveis.")
    )

    data class Change(val title: String, val description: String)
}
