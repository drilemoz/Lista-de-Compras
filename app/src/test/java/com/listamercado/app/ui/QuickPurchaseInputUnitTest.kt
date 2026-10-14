package com.listamercado.app.ui

import org.junit.Assert.*
import org.junit.Test

class QuickPurchaseInputUnitTest {
    @Test fun acceptsBrazilianMoneyAndQuantities() {
        assertEquals(1234.50, QuickPurchaseInput.parseNonNegative("1.234,50")!!, 0.0000001)
        assertEquals(13.45, QuickPurchaseInput.parseNonNegative("13.45")!!, 0.0000001)
        assertEquals(0.0, QuickPurchaseInput.parseNonNegative("0")!!, 0.0000001)
    }

    @Test fun rejectsIncompleteAndUnsafeEntries() {
        for (raw in listOf(null, "", " ", "12,", "5,3.2", "-1", "NaN", "Infinity", "1.2.3", "1.000.000")) {
            assertNull("Invalid entry: $raw", QuickPurchaseInput.parseNonNegative(raw))
        }
    }
}
