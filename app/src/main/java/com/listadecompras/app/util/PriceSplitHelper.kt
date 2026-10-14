package com.listadecompras.app.util

import java.math.BigDecimal
import java.math.RoundingMode

/** Mantém o total exato do combo, sem multiplicar um valor por unidade já arredondado. */
object PriceSplitHelper {
    fun isValid(total: Double, count: Int): Boolean =
        total.isFinite() && total > 0.0 && total <= 100_000_000.0 && count in 2..999

    fun unitPrice(total: Double, count: Int): Double {
        require(isValid(total, count)) { "Informe um total positivo e de 2 a 999 unidades." }
        return BigDecimal.valueOf(total).divide(BigDecimal.valueOf(count.toLong()), 12, RoundingMode.HALF_UP).toDouble()
    }

    fun roundedUnitPrice(total: Double, count: Int): Double =
        BigDecimal.valueOf(unitPrice(total, count)).setScale(2, RoundingMode.HALF_UP).toDouble()

    fun subtotal(total: Double, count: Int, quantity: Double): Double {
        require(isValid(total, count) && quantity.isFinite() && quantity >= 0.0)
        return BigDecimal.valueOf(total).multiply(BigDecimal.valueOf(quantity))
            .divide(BigDecimal.valueOf(count.toLong()), 2, RoundingMode.HALF_UP).toDouble()
    }
}
