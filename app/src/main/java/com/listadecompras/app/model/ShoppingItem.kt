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
    var comboUnitCount: Int? = null,
    var imageFile: String = "",
    var productUrl: String = "",
    var storeName: String = "",
    var variant: String = "",
    var installmentCount: Int = 0,
    var installmentAmount: Double = 0.0,
    var downPayment: Double = 0.0,
    var shipping: Double = 0.0,
    var extraFees: Double = 0.0,
    var paidInstallments: Int = 0,
    var firstDueDate: String = "",
    var alternativeStore: String = "",
    var alternativeUrl: String = "",
    var alternativeCashPrice: Double? = null,
    var alternativeInstallments: Int = 0,
    var alternativeInstallmentAmount: Double = 0.0,
    var alternativeDownPayment: Double = 0.0,
    var alternativeShipping: Double = 0.0,
    var alternativeFees: Double = 0.0
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

    val shopCalculation: com.listadecompras.app.util.InstallmentCalculator.Result?
        get() = com.listadecompras.app.util.InstallmentCalculator.calculate(
            unitPrice, installmentCount, installmentAmount, downPayment, shipping, extraFees, paidInstallments)

    /** Uses the exact original combo total when available, avoiding rounding it per piece. */
    val shopCashSubtotal: Double
        get() = subtotal + com.listadecompras.app.util.InstallmentCalculator.totalForQuantity(shipping, quantity)

    val shopFinancedSubtotal: Double
        get() = if (installmentCount > 0) com.listadecompras.app.util.InstallmentCalculator.totalForQuantity(
            shopCalculation?.financedTotal ?: unitPrice, quantity)
        else shopCashSubtotal + com.listadecompras.app.util.InstallmentCalculator.totalForQuantity(extraFees, quantity)

    val shopPurchasedSubtotal: Double
        get() = if (installmentCount > 0) com.listadecompras.app.util.InstallmentCalculator.totalForQuantity(
            shopCalculation?.financedTotal ?: unitPrice, purchasedQuantity.coerceAtLeast(0.0))
        else purchasedSubtotal + com.listadecompras.app.util.InstallmentCalculator.totalForQuantity(
            shipping + extraFees, purchasedQuantity.coerceAtLeast(0.0))

    private fun baseQuantity(value: Double): Double = if (unit == "g" || unit == "mL") value / 1000.0 else value

    private fun hasValidCombo(): Boolean = comboTotalPrice != null && comboUnitCount != null &&
        com.listadecompras.app.util.PriceSplitHelper.isValid(comboTotalPrice!!, comboUnitCount!!)

    /** Garante uma quantidade real útil ao concluir uma compra antiga/sem preenchimento. */
    fun ensurePurchasedQuantity() {
        if (purchasedQuantity <= 0.0) purchasedQuantity = quantity.coerceAtLeast(0.01)
    }
}
