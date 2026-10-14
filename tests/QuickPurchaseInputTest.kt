package com.listadecompras.app.ui

fun main() {
    check(QuickPurchaseInput.parseNonNegative(null) == null)
    check(QuickPurchaseInput.parseNonNegative("") == null)
    check(QuickPurchaseInput.parseNonNegative("  ") == null)
    check(QuickPurchaseInput.parseNonNegative("12,") == null)
    check(QuickPurchaseInput.parseNonNegative("xyz") == null)
    check(QuickPurchaseInput.parseNonNegative("-5") == null)
    check(QuickPurchaseInput.parseNonNegative("NaN") == null)
    check(QuickPurchaseInput.parseNonNegative("0") == 0.0)
    check(QuickPurchaseInput.parseNonNegative("13,45") == 13.45)
    check(QuickPurchaseInput.parseNonNegative("1.234,50") == 1234.50)
    check(QuickPurchaseInput.parseNonNegative("13.45") == 13.45)
    check(QuickPurchaseInput.parseNonNegative("5,3.2") == null)
    println("12 testes de entradas de compra aprovados")
}
