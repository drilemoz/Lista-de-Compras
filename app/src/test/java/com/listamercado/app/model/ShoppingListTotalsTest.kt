package com.listamercado.app.model

import org.junit.Assert.*
import org.junit.Test

class ShoppingListTotalsTest {
    @Test fun plannedAndPurchasedAmountsAreIndependent() {
        val rice = ShoppingItem(id = 1L, name = "Arroz", quantity = 5.0,
            purchasedQuantity = 3.0, unitPrice = 4.0, purchased = true)
        val eggs = ShoppingItem(id = 2L, name = "Ovos", quantity = 2.0,
            unitPrice = 12.0, purchased = false)
        val list = ShoppingList(name = "Teste", budget = 50.0, items = mutableListOf(rice, eggs))
        assertEquals(44.0, list.estimatedTotal, 0.0000001)
        assertEquals(12.0, list.purchasedTotal, 0.0000001)
        assertEquals(1, list.purchasedCount)
        assertEquals(6.0, list.budgetRemaining, 0.0000001)
    }

    @Test fun finishingWithoutMeasuredQuantityUsesPlannedQuantity() {
        val item = ShoppingItem(name = "Farinha", quantity = 2.5, unitPrice = 3.0)
        item.ensurePurchasedQuantity()
        assertEquals(2.5, item.purchasedQuantity, 0.0000001)
        item.ensurePurchasedQuantity()
        assertEquals(2.5, item.purchasedQuantity, 0.0000001)
    }
}
