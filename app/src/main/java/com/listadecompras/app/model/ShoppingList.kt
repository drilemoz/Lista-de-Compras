package com.listadecompras.app.model

data class ShoppingList(
    val id: Long = System.currentTimeMillis(),
    var name: String,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var budget: Double = 0.0,
    var marketKey: String? = null,
    var marketName: String? = null,
    val items: MutableList<ShoppingItem> = mutableListOf(),
    var listType: ListType = ListType.SUPERMARKET
) {
    /** Estimativa usa a quantidade planejada. */
    val estimatedTotal: Double
        get() = if (listType == ListType.SHOPPING) financedTotal else items.sumOf { it.subtotal }

    /** Carrinho usa somente a quantidade realmente comprada dos itens concluídos. */
    val purchasedTotal: Double
        get() = items.filter { it.purchased }.sumOf {
            if (listType == ListType.SHOPPING) it.shopPurchasedSubtotal else it.purchasedSubtotal
        }

    val cashTotal: Double
        get() = if (listType == ListType.SHOPPING) items.sumOf { it.shopCashSubtotal } else items.sumOf { it.subtotal }

    val financedTotal: Double
        get() = if (listType == ListType.SHOPPING) items.sumOf { it.shopFinancedSubtotal } else items.sumOf { it.subtotal }

    /** Sum of the declared per-product monthly payments, assuming simultaneous plans. */
    val monthlyCommitment: Double
        get() = if (listType == ListType.SHOPPING) items.sumOf {
            com.listadecompras.app.util.InstallmentCalculator.totalForQuantity(
                if (it.paidInstallments < it.installmentCount) (it.shopCalculation?.monthlyCommitment ?: 0.0) else 0.0, it.quantity)
        } else 0.0

    val purchasedCount: Int
        get() = items.count { it.purchased }

    val budgetRemaining: Double
        get() = budget - estimatedTotal
}
