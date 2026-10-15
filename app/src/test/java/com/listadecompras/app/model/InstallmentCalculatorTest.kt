package com.listadecompras.app.model

import com.listadecompras.app.util.InstallmentCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallmentCalculatorTest {
    @Test fun totalAndWarningAreNotMonthlyInterest() {
        val result = InstallmentCalculator.calculate(1000.0, 10, 160.0)!!
        assertEquals(1000.0, result.cashTotal, 0.0001)
        assertEquals(1600.0, result.financedTotal, 0.0001)
        assertEquals(60.0, result.extraPercent, 0.0001)
        assertEquals(160.0, result.monthlyCommitment, 0.0001)
        assertEquals(10, result.installmentsRemaining)
    }

    @Test fun entryShippingPaidAndQuantities() {
        val result = InstallmentCalculator.calculate(1000.0, 10, 110.0, 100.0, 35.0, 15.0, 3)!!
        assertEquals(1035.0, result.cashTotal, 0.001)
        assertEquals(1250.0, result.financedTotal, 0.001)
        assertEquals(215.0, result.extraCost, 0.001)
        assertEquals(7, result.installmentsRemaining)
        assertEquals(2500.0, InstallmentCalculator.totalForQuantity(result.financedTotal, 2.0), 0.001)
    }

    @Test fun dueDatesAndCompletion() {
        assertEquals("2026-12-15", InstallmentCalculator.nextDueDate("2026-10-15", 2, 10))
        assertNull(InstallmentCalculator.nextDueDate("2026-10-15", 10, 10))
        assertFalse(InstallmentCalculator.validDueDate("15/10/2026"))
        assertTrue(InstallmentCalculator.validDueDate("2026-10-15"))
    }

    @Test fun rejectsInvalidPlansAndLinks() {
        assertNull(InstallmentCalculator.calculate(1000.0, 121, 10.0))
        assertNull(InstallmentCalculator.calculate(1000.0, 10, 10.0, paidInstallments = 11))
        assertNull(InstallmentCalculator.calculate(-1.0, 0, 0.0))
        assertFalse(InstallmentCalculator.validUrl("javascript:alert(1)"))
        assertFalse(InstallmentCalculator.validUrl("https://usuario:senha@example.com/"))
        assertTrue(InstallmentCalculator.validUrl("https://loja.example/produto?id=1"))
    }

    @Test fun shoppingTotalsDoNotChangeSupermarketTotals() {
        val item = ShoppingItem(name = "Celular", quantity = 2.0, unitPrice = 1000.0,
            installmentCount = 10, installmentAmount = 160.0)
        val shopping = ShoppingList(name = "Shopping", listType = ListType.SHOPPING,
            items = mutableListOf(item))
        assertEquals(2000.0, shopping.cashTotal, 0.001)
        assertEquals(3200.0, shopping.financedTotal, 0.001)
        assertEquals(320.0, shopping.monthlyCommitment, 0.001)
        assertEquals(3200.0, shopping.estimatedTotal, 0.001)
        val combo = ShoppingItem(name="Combo", quantity=2.0, unitPrice=14.79 / 2,
            comboTotalPrice=14.79, comboUnitCount=2)
        assertEquals(14.79, ShoppingList(name="Combo", listType=ListType.SHOPPING,
            items=mutableListOf(combo)).estimatedTotal, 0.0001)
        val supermarket = shopping.copy(listType = ListType.SUPERMARKET)
        assertEquals(2000.0, supermarket.estimatedTotal, 0.001)
        assertEquals(ListType.SUPERMARKET, ListType.fromStored(null))
    }
}
