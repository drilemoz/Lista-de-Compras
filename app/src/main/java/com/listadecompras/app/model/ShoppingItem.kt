package com.listadecompras.app.model

data class ShoppingItem(
    val id: Long = System.currentTimeMillis(),
    var name: String,
    var quantity: Double = 1.0,
    var purchasedQuantity: Double = 0.0,
    var unit: String = "un",
    var unitPrice: Double = 0.0,
    var category: String = "Outros",
    var note: String = "",
    var purchased: Boolean = false,
    /** Valor total da promoção (por exemplo, 14,79 por 2 unidades). */
    var comboTotalPrice: Double? = null,
    /** Número de unidades na promoção. Nulo em produtos sem combo. */
    var comboUnitCount: Int? = null
) {
    /** Quantidade planejada × preço unitário. Usado na estimativa da lista. */
    val subtotal: Double
        get() = if (hasValidCombo())
            com.listadecompras.app.util.PriceSplitHelper.subtotal(comboTotalPrice!!, comboUnitCount!!, baseQuantity(quantity))
        else quantity * unitPrice

    /** Quantidade realmente comprada × preço unitário. */
    val purchasedSubtotal: Double
        get() = if (hasValidCombo())
            com.listadecompras.app.util.PriceSplitHelper.subtotal(comboTotalPrice!!, comboUnitCount!!, baseQuantity(purchasedQuantity.coerceAtLeast(0.0)))
        else purchasedQuantity.coerceAtLeast(0.0) * unitPrice

    private fun baseQuantity(value: Double): Double = if (unit == "g" || unit == "mL") value / 1000.0 else value

    private fun hasValidCombo(): Boolean = comboTotalPrice != null && comboUnitCount != null &&
        com.listadecompras.app.util.PriceSplitHelper.isValid(comboTotalPrice!!, comboUnitCount!!)

    /** Garante uma quantidade real útil ao concluir uma compra antiga/sem preenchimento. */
    fun ensurePurchasedQuantity() {
        if (purchasedQuantity <= 0.0) purchasedQuantity = quantity.coerceAtLeast(0.01)
    }
}
