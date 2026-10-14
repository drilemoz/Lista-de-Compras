package com.listamercado.app.ui

import com.listamercado.app.model.ShoppingItem
import com.listamercado.app.model.ShoppingList

fun main() {
    val allowed = listOf(MapResourcePolicy.LOCAL_MAP,
        "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js",
        "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css",
        "https://tile.openstreetmap.org/13/1234/5678.png")
    allowed.forEach { check(MapResourcePolicy.isAllowedResource(it)) }
    val blocked = listOf(null, "https://example.com/script.js", "http://unpkg.com/leaflet@1.9.4/dist/leaflet.js",
        "https://unpkg.com/leaflet@1.9.5/dist/leaflet.js", "https://tile.openstreetmap.org.bad.tld/13/1234/5678.png",
        "https://tile.openstreetmap.org/a/b/c.png", "https://tile.openstreetmap.org/12/34/56.png?x=y",
        "file:///sdcard/arquivo.html", "javascript:alert(1)")
    blocked.forEach { check(!MapResourcePolicy.isAllowedResource(it)) }
    val item = ShoppingItem(name="Arroz", quantity=5.0, purchasedQuantity=3.0, unitPrice=4.0, purchased=true)
    val another = ShoppingItem(name="Ovos", quantity=2.0, unitPrice=12.0)
    val list = ShoppingList(name="Mercado", budget=50.0, items=mutableListOf(item, another))
    check(list.estimatedTotal == 44.0)
    check(list.purchasedTotal == 12.0)
    check(list.purchasedCount == 1)
    check(list.budgetRemaining == 6.0)
    val before = list.copy(items = list.items.map { it.copy() }.toMutableList())
    item.unitPrice = 5.0
    check(before.items[0].unitPrice == 4.0) // snapshot para DiffUtil
    val noQuantity = ShoppingItem(name="Farinha", quantity=2.5, unitPrice=3.0)
    noQuantity.ensurePurchasedQuantity()
    check(noQuantity.purchasedQuantity == 2.5)
    check(QuickPurchaseInput.parseNonNegative("1.234,50") == 1234.5)
    check(QuickPurchaseInput.parseNonNegative("12,") == null)
    println("Testes offline finais aprovados: 13 verificações de mapa, valores, snapshots e carrinho")
}
