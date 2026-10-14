package com.listadecompras.app.model

import com.listadecompras.app.util.PriceSplitHelper
import org.junit.Assert.*
import org.junit.Test

class PriceSplitHelperTest {
    @Test fun promoDoesNotLoseCentOnTwoItems() {
        val price = PriceSplitHelper.unitPrice(14.79, 2)
        assertEquals(7.395, price, 0.00000000001)
        assertEquals(7.40, PriceSplitHelper.roundedUnitPrice(14.79, 2), 0.001)
        val item = ShoppingItem(name = "Retrovisor", quantity = 2.0, purchasedQuantity = 2.0,
            unitPrice = price, comboTotalPrice = 14.79, comboUnitCount = 2, purchased = true)
        assertEquals(14.79, item.subtotal, 0.00001)
        assertEquals(14.79, item.purchasedSubtotal, 0.00001)
        assertEquals(7.40, item.copy(quantity = 1.0).subtotal, 0.00001)
        assertEquals(22.19, item.copy(quantity = 3.0).subtotal, 0.00001)
    }

    @Test fun kiloAndGramCombosAreConsistent() {
        val price = PriceSplitHelper.unitPrice(14.79, 2)
        val product = ShoppingItem(name = "Farinha", unit = "g", quantity = 2000.0,
            unitPrice = price / 1000.0, comboTotalPrice = 14.79, comboUnitCount = 2)
        assertEquals(14.79, product.subtotal, 0.00001)
    }

    @Test fun rejectsInvalidCombosAndKeepsOrdinaryPrices() {
        assertFalse(PriceSplitHelper.isValid(0.0, 2))
        assertFalse(PriceSplitHelper.isValid(Double.NaN, 2))
        assertFalse(PriceSplitHelper.isValid(8.0, 1))
        assertFalse(PriceSplitHelper.isValid(8.0, 1000))
        val regular = ShoppingItem(name = "Produto", quantity = 3.0, unitPrice = 1.25)
        assertEquals(3.75, regular.subtotal, 0.00001)
    }

    @Test fun purchasedQuantityIsIndependentOfPlanned() {
        val price = PriceSplitHelper.unitPrice(12.99, 2)
        val item = ShoppingItem(name = "Pães", quantity = 4.0, purchasedQuantity = 2.0,
            purchased = true, unitPrice = price, comboTotalPrice = 12.99, comboUnitCount = 2)
        assertEquals(25.98, item.subtotal, 0.00001)
        assertEquals(12.99, item.purchasedSubtotal, 0.00001)
    }
}
