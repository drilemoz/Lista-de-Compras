package com.listadecompras.app.model

import com.listadecompras.app.util.InstallmentCalculator

fun main() {
    val r = InstallmentCalculator.calculate(1000.0, 10, 160.0)!!
    check(r.financedTotal == 1600.0 && r.extraPercent == 60.0)
    val r2 = InstallmentCalculator.calculate(1000.0, 10, 110.0, 100.0, 35.0, 15.0, 3)!!
    check(r2.financedTotal == 1250.0 && r2.installmentsRemaining == 7)
    check(!InstallmentCalculator.validUrl("javascript:alert(1)"))
    check(InstallmentCalculator.nextDueDate("2026-10-15", 2, 10) == "2026-12-15")
    val i = ShoppingItem(name="Notebook", unitPrice=1000.0, quantity=2.0, installmentCount=10, installmentAmount=160.0)
    val l = ShoppingList(name="Tecnologia", listType=ListType.SHOPPING, items=mutableListOf(i))
    check(l.estimatedTotal == 3200.0 && l.cashTotal == 2000.0 && l.monthlyCommitment == 320.0)
    check(l.copy(listType=ListType.SUPERMARKET).estimatedTotal == 2000.0)
    val combo = ShoppingItem(name="Combo", quantity=2.0, unitPrice=14.79 / 2, comboTotalPrice=14.79, comboUnitCount=2)
    val comboList = ShoppingList(name="Promo", listType=ListType.SHOPPING, items=mutableListOf(combo))
    check(comboList.estimatedTotal == 14.79)
    println("Shopping mode: 10 checks OK")
}
