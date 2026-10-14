package com.listadecompras.app.util

import com.listadecompras.app.model.ShoppingItem
import java.text.NumberFormat

object PriceUnitHelper {
    fun editorAmount(internalPrice: Double, unit: String): Double = when (unit) {
        "g", "mL" -> internalPrice * 1000.0
        else -> internalPrice
    }

    fun internalAmount(displayPrice: Double, unit: String): Double = when (unit) {
        "g", "mL" -> displayPrice / 1000.0
        else -> displayPrice
    }

    fun priceUnit(unit: String): String = when (unit) {
        "g" -> "kg"
        "mL" -> "L"
        else -> unit
    }

    fun inputHint(unit: String): String = when (unit) {
        "g" -> "Preço por kg"
        "mL" -> "Preço por L"
        else -> "Preço unitário"
    }

    fun displayUnitPrice(internalPrice: Double, unit: String): Pair<Double, String> =
        editorAmount(internalPrice, unit) to priceUnit(unit)

    fun normalizedPrice(item: ShoppingItem): Double = editorAmount(item.unitPrice, item.unit)

    fun normalizedUnit(item: ShoppingItem): String = priceUnit(item.unit)

    fun formattedUnitPrice(item: ShoppingItem, currency: NumberFormat): String {
        if (item.unitPrice <= 0.0) return "Sem preço"
        val value = java.math.BigDecimal.valueOf(normalizedPrice(item))
            .setScale(2, java.math.RoundingMode.HALF_UP)
        return "${currency.format(value)}/${normalizedUnit(item)}"
    }
}
