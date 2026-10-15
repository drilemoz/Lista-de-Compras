package com.listadecompras.app.ui

import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.listadecompras.app.R
import com.listadecompras.app.model.ShoppingItem
import com.listadecompras.app.util.CurrencyTextWatcher
import com.listadecompras.app.util.InstallmentCalculator
import java.text.NumberFormat
import java.util.Locale

/** Shopping-only payment and alternative-offer form. Fields are kept outside ItemDialog. */
internal class ShoppingFieldsForm(
    view: View,
    private val priceWatcher: CurrencyTextWatcher,
    private val currency: NumberFormat
) {
    val section: View = view.findViewById(R.id.sectionShopping)
    private val store: TextInputEditText = view.findViewById(R.id.inputShopStore)
    private val link: TextInputEditText = view.findViewById(R.id.inputShopUrl)
    private val variant: TextInputEditText = view.findViewById(R.id.inputShopVariant)
    private val shipping: TextInputEditText = view.findViewById(R.id.inputShopShipping)
    private val fees: TextInputEditText = view.findViewById(R.id.inputShopFees)
    private val installments: MaterialCheckBox = view.findViewById(R.id.checkShopInstallments)
    private val installmentSection: View = view.findViewById(R.id.sectionShopInstallments)
    private val count: TextInputEditText = view.findViewById(R.id.inputShopCount)
    private val amount: TextInputEditText = view.findViewById(R.id.inputShopInstallment)
    private val down: TextInputEditText = view.findViewById(R.id.inputShopDown)
    private val paid: TextInputEditText = view.findViewById(R.id.inputShopPaid)
    private val due: TextInputEditText = view.findViewById(R.id.inputShopDue)
    private val summary: TextView = view.findViewById(R.id.textShopSummary)
    private val warning: TextView = view.findViewById(R.id.textShopWarning)
    private val compare: MaterialCheckBox = view.findViewById(R.id.checkAltOffer)
    private val compareSection: View = view.findViewById(R.id.sectionAltOffer)
    private val altStore: TextInputEditText = view.findViewById(R.id.inputAltStore)
    private val altLink: TextInputEditText = view.findViewById(R.id.inputAltUrl)
    private val altCash: TextInputEditText = view.findViewById(R.id.inputAltCash)
    private val altCount: TextInputEditText = view.findViewById(R.id.inputAltCount)
    private val altAmount: TextInputEditText = view.findViewById(R.id.inputAltInstallment)
    private val altCompare: TextView = view.findViewById(R.id.textAltCompare)
    private val altDown: TextInputEditText = view.findViewById(R.id.inputAltDown)
    private val altShipping: TextInputEditText = view.findViewById(R.id.inputAltShipping)
    private val altFees: TextInputEditText = view.findViewById(R.id.inputAltFees)
    private val financialInputs = listOf(shipping, fees, amount, down, altCash, altAmount, altDown, altShipping, altFees)
    private val watchers = financialInputs.associateWith { input ->
        CurrencyTextWatcher(input).also { watcher -> input.addTextChangedListener(watcher) }
    }

    private fun money(input: TextInputEditText) = watchers.getValue(input).amount()
    private fun setMoney(input: TextInputEditText, value: Double) = watchers.getValue(input).setAmount(value)
    private fun format(value: Double) = currency.format(value)

    init {
        installments.setOnCheckedChangeListener { _, checked ->
            installmentSection.visibility = if (checked) View.VISIBLE else View.GONE
            update()
        }
        compare.setOnCheckedChangeListener { _, checked ->
            compareSection.visibility = if (checked) View.VISIBLE else View.GONE
            update()
        }
        (financialInputs + listOf(count, paid, altCount, due)).forEach { input -> input.doAfterTextChanged { update() } }
    }

    fun update() {
        val n = if (installments.isChecked) count.text?.toString()?.toIntOrNull() ?: 0 else 0
        val result = InstallmentCalculator.calculate(priceWatcher.amount(), n,
            if (installments.isChecked) money(amount) else 0.0,
            if (installments.isChecked) money(down) else 0.0,
            money(shipping), money(fees),
            if (installments.isChecked) paid.text?.toString()?.toIntOrNull() ?: 0 else 0)
        if (result != null) {
            val next = InstallmentCalculator.nextDueDate(due.text?.toString()?.trim().orEmpty(),
                paid.text?.toString()?.toIntOrNull() ?: 0, n)
            summary.text = "À vista ${format(result.cashTotal)} • total ${format(result.financedTotal)}" +
                (if (n > 0) "\n$n × ${format(money(amount))} • ${result.installmentsRemaining} restantes" else "\nPagamento à vista") +
                (next?.let { "\nPróximo vencimento: $it" } ?: "")
            warning.text = when {
                result.cashTotal <= 0.0 && n > 0 -> "Informe o preço à vista para comparar."
                result.extraPercent > 50.0 -> "⚠ Acréscimo total acima de 50%: ${format(result.extraCost)} (${String.format(Locale("pt", "BR"), "%.1f", result.extraPercent)}%)."
                result.extraCost > 0.0 -> "Acréscimo total: ${format(result.extraCost)} (${String.format(Locale("pt", "BR"), "%.1f", result.extraPercent)}%)."
                result.extraCost < 0.0 -> "Economia de ${format(-result.extraCost)}."
                else -> "Sem acréscimo em relação ao preço à vista."
            }
            warning.visibility = View.VISIBLE
        } else {
            summary.text = "Revise os valores e a quantidade de parcelas."
            warning.visibility = View.GONE
        }
        if (compare.isChecked) {
            val altN = altCount.text?.toString()?.toIntOrNull() ?: 0
            val alternativeTotal = if (altN > 0) {
                (altN * money(altAmount)) + money(altDown) + money(altShipping) + money(altFees)
            } else money(altCash) + money(altShipping) + money(altFees)
            val primary = result?.financedTotal ?: 0.0
            altCompare.text = when {
                alternativeTotal <= 0.0 || primary <= 0.0 -> "Preencha as duas ofertas para comparar."
                alternativeTotal < primary -> "Outra oferta ${format(alternativeTotal)}: economiza ${format(primary - alternativeTotal)}."
                else -> "Outra oferta ${format(alternativeTotal)}: custa ${format(alternativeTotal - primary)} a mais."
            }
        }
    }

    fun restore(item: ShoppingItem) {
        store.setText(item.storeName)
        link.setText(item.productUrl)
        variant.setText(item.variant)
        setMoney(shipping, item.shipping)
        setMoney(fees, item.extraFees)
        if (item.installmentCount > 0) {
            installments.isChecked = true
            count.setText(item.installmentCount.toString())
            setMoney(amount, item.installmentAmount)
            setMoney(down, item.downPayment)
            paid.setText(item.paidInstallments.toString())
            due.setText(item.firstDueDate)
        }
        if (item.alternativeCashPrice != null || item.alternativeInstallments > 0 || item.alternativeUrl.isNotBlank()) {
            compare.isChecked = true
            altStore.setText(item.alternativeStore)
            altLink.setText(item.alternativeUrl)
            setMoney(altCash, item.alternativeCashPrice ?: 0.0)
            altCount.setText(if (item.alternativeInstallments > 0) item.alternativeInstallments.toString() else "")
            setMoney(altAmount, item.alternativeInstallmentAmount)
            setMoney(altDown, item.alternativeDownPayment)
            setMoney(altShipping, item.alternativeShipping)
            setMoney(altFees, item.alternativeFees)
        }
        update()
    }

    fun validate(): Boolean {
        val n = if (installments.isChecked) count.text?.toString()?.toIntOrNull() ?: 0 else 0
        val paidN = if (installments.isChecked) paid.text?.toString()?.toIntOrNull() ?: 0 else 0
        if (!InstallmentCalculator.validUrl(link.text?.toString().orEmpty()) ||
            !InstallmentCalculator.validUrl(altLink.text?.toString().orEmpty())) {
            summary.text = "Informe links HTTP ou HTTPS válidos, sem credenciais."
            return false
        }
        if (listOf(store, variant, altStore).any { it.text?.toString().orEmpty().length > 120 }) {
            summary.text = "Loja e variante devem ter até 120 caracteres."
            return false
        }
        if (installments.isChecked && !InstallmentCalculator.validDueDate(due.text?.toString()?.trim().orEmpty())) {
            summary.text = "Primeiro vencimento deve ser AAAA-MM-DD."
            return false
        }
        if (installments.isChecked && priceWatcher.amount() <= 0.0) {
            summary.text = "Informe o preço à vista para calcular o acréscimo."
            return false
        }
        if (installments.isChecked && (n !in 1..120 || money(amount) <= 0.0)) {
            summary.text = "Informe 1 a 120 parcelas com valor positivo."
            return false
        }
        if (InstallmentCalculator.calculate(priceWatcher.amount(), n,
                if (installments.isChecked) money(amount) else 0.0,
                if (installments.isChecked) money(down) else 0.0,
                money(shipping), money(fees), paidN) == null) {
            summary.text = "Revise valor, entrada, parcelas e quantidade já paga."
            return false
        }
        if (compare.isChecked) {
            val altN = altCount.text?.toString()?.toIntOrNull() ?: 0
            if (altN !in 0..120 || (altN > 0 && money(altAmount) <= 0.0) ||
                (altN == 0 && money(altAmount) > 0.0)) {
                altCompare.text = "Revise as parcelas da outra oferta."
                return false
            }
        }
        return true
    }

    fun applyTo(item: ShoppingItem) {
        item.storeName = store.text?.toString()?.trim().orEmpty()
        item.productUrl = link.text?.toString()?.trim().orEmpty()
        item.variant = variant.text?.toString()?.trim().orEmpty()
        item.installmentCount = if (installments.isChecked) count.text?.toString()?.toIntOrNull() ?: 0 else 0
        item.installmentAmount = if (installments.isChecked) money(amount) else 0.0
        item.downPayment = if (installments.isChecked) money(down) else 0.0
        item.shipping = money(shipping)
        item.extraFees = money(fees)
        item.paidInstallments = if (installments.isChecked) paid.text?.toString()?.toIntOrNull() ?: 0 else 0
        item.firstDueDate = if (installments.isChecked) due.text?.toString()?.trim().orEmpty() else ""
        item.alternativeStore = if (compare.isChecked) altStore.text?.toString()?.trim().orEmpty() else ""
        item.alternativeUrl = if (compare.isChecked) altLink.text?.toString()?.trim().orEmpty() else ""
        item.alternativeCashPrice = if (compare.isChecked) money(altCash).takeIf { it > 0.0 } else null
        item.alternativeInstallments = if (compare.isChecked) altCount.text?.toString()?.toIntOrNull() ?: 0 else 0
        item.alternativeInstallmentAmount = if (compare.isChecked && item.alternativeInstallments > 0) money(altAmount) else 0.0
        item.alternativeDownPayment = if (compare.isChecked) money(altDown) else 0.0
        item.alternativeShipping = if (compare.isChecked) money(altShipping) else 0.0
        item.alternativeFees = if (compare.isChecked) money(altFees) else 0.0
    }
}
