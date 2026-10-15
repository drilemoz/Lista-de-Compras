package com.listadecompras.app.util

import java.math.BigDecimal
import java.math.RoundingMode

/** Values are for one purchased unit. Displayed total surcharge is NOT a monthly interest rate. */
object InstallmentCalculator {
    private fun money(value: Double): BigDecimal = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP)
    fun validAmount(value: Double): Boolean = value.isFinite() && value >= 0.0 && value <= 100_000_000.0

    fun validUrl(raw: String): Boolean {
        if (raw.isBlank()) return true
        if (raw.length > 2048 || raw.any { it.isWhitespace() || it.isISOControl() }) return false
        val uri = runCatching { java.net.URI(raw) }.getOrNull() ?: return false
        return (uri.scheme == "https" || uri.scheme == "http") && !uri.host.isNullOrBlank() && uri.userInfo == null
    }

    fun validDueDate(iso: String): Boolean = iso.isBlank() ||
        runCatching { java.time.LocalDate.parse(iso) }.isSuccess

    fun nextDueDate(iso: String, paidInstallments: Int, totalInstallments: Int): String? {
        if (!validDueDate(iso) || iso.isBlank() || paidInstallments !in 0 until totalInstallments) return null
        return java.time.LocalDate.parse(iso).plusMonths(paidInstallments.toLong()).toString()
    }

    data class Result(
        val cashTotal: Double,
        val financedTotal: Double,
        val extraCost: Double,
        val extraPercent: Double,
        val monthlyCommitment: Double,
        val installmentsRemaining: Int
    )

    fun calculate(
        cashUnitPrice: Double, installments: Int, installmentAmount: Double,
        downPayment: Double = 0.0, shipping: Double = 0.0, fees: Double = 0.0,
        paidInstallments: Int = 0
    ): Result? {
        if (!listOf(cashUnitPrice, installmentAmount, downPayment, shipping, fees).all(::validAmount) ||
            installments !in 0..120 || paidInstallments !in 0..installments ||
            (installments > 0 && installmentAmount <= 0.0) || (installments == 0 && (installmentAmount > 0 || downPayment > 0 || paidInstallments > 0))) return null
        val cash = money(cashUnitPrice).add(money(shipping))
        val financed = if (installments > 0) {
            money(downPayment).add(money(installmentAmount).multiply(BigDecimal(installments)))
                .add(money(shipping)).add(money(fees))
        } else cash.add(money(fees))
        val extra = financed.subtract(cash)
        val percent = if (cash.signum() > 0) extra.multiply(BigDecimal(100))
            .divide(cash, 4, RoundingMode.HALF_UP).toDouble() else 0.0
        return Result(cash.toDouble(), financed.toDouble(), extra.toDouble(), percent,
            if (installments > 0) money(installmentAmount).toDouble() else 0.0, installments - paidInstallments)
    }

    fun totalForQuantity(value: Double, quantity: Double): Double {
        if (!validAmount(value) || !quantity.isFinite() || quantity < 0.0) return 0.0
        return money(value).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}
