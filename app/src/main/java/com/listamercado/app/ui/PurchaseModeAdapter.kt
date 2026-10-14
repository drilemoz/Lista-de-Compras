package com.listamercado.app.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.listamercado.app.R
import com.listamercado.app.model.CatalogProduct
import com.listamercado.app.model.ShoppingItem
import com.listamercado.app.util.PriceTargetHelper
import com.listamercado.app.util.PriceUnitHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

class PurchaseModeAdapter(
    private val onPurchased: (ShoppingItem) -> Unit,
    private val onPriceChanged: (ShoppingItem, Double) -> Unit,
    private val onPurchasedQuantityChanged: (ShoppingItem, Double) -> Unit
) : RecyclerView.Adapter<PurchaseModeAdapter.Holder>() {
    private val items = mutableListOf<ShoppingItem>()
    private var catalogByName: Map<String, CatalogProduct> = emptyMap()
    private val currency = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val quantityFormat = DecimalFormat("0.##", DecimalFormatSymbols(Locale("pt", "BR")))
    private val priceFormat = DecimalFormat("0.00", DecimalFormatSymbols(Locale("pt", "BR")))
    private data class EditDraft(val quantity: String, val total: String, val quantityDirty: Boolean, val priceDirty: Boolean)
    private val drafts = mutableMapOf<Long, EditDraft>()

    /** Incomplete numeric entries stay drafts, never saved as a zero price. */
    fun restoreDrafts(state: Bundle?) {
        val ids = state?.getLongArray("purchase_draft_ids") ?: return
        val quantities = state.getStringArrayList("purchase_draft_quantities") ?: return
        val totals = state.getStringArrayList("purchase_draft_totals") ?: return
        val flags = state.getIntArray("purchase_draft_flags") ?: return
        if (ids.size != quantities.size || ids.size != totals.size || ids.size != flags.size) return
        ids.indices.forEach { index ->
            drafts[ids[index]] = EditDraft(quantities[index], totals[index], flags[index] and 1 != 0, flags[index] and 2 != 0)
        }
    }

    fun saveDrafts(state: Bundle) {
        state.putLongArray("purchase_draft_ids", drafts.keys.toLongArray())
        state.putStringArrayList("purchase_draft_quantities", ArrayList(drafts.values.map { it.quantity }))
        state.putStringArrayList("purchase_draft_totals", ArrayList(drafts.values.map { it.total }))
        state.putIntArray("purchase_draft_flags", drafts.values.map { (if (it.quantityDirty) 1 else 0) + (if (it.priceDirty) 2 else 0) }.toIntArray())
    }

    fun discardDraft(itemId: Long) { drafts.remove(itemId) }

    fun firstUnresolvedDraftPosition(): Int = items.indexOfFirst { item ->
        drafts[item.id]?.let { it.quantityDirty || it.priceDirty } == true
    }

    fun submitList(newItems: List<ShoppingItem>, catalogProducts: List<CatalogProduct> = emptyList()) {
        items.clear()
        items.addAll(newItems)
        catalogByName = catalogProducts.associateBy { it.normalizedName }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_purchase_mode, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: Holder) {
        holder.commitEdits()
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: Holder) {
        holder.commitEdits()
        super.onViewDetachedFromWindow(holder)
    }

    /** Commits only complete, valid drafts in visible rows before leaving the screen. */
    fun commitVisibleEdits(recycler: RecyclerView): Boolean {
        var allValid = true
        for (index in 0 until recycler.childCount) {
            val holder = recycler.getChildViewHolder(recycler.getChildAt(index)) as? Holder ?: continue
            if (!holder.commitEdits()) allValid = false
        }
        return allValid
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val check: MaterialCheckBox = view.findViewById(R.id.checkPurchaseMode)
        private val name: TextView = view.findViewById(R.id.textPurchaseItemName)
        private val quantity: TextView = view.findViewById(R.id.textPurchaseItemQuantity)
        private val category: TextView = view.findViewById(R.id.textPurchaseItemCategory)
        private val purchasedQuantityLayout: TextInputLayout = view.findViewById(R.id.layoutPurchasedQuantity)
        private val purchasedQuantity: TextInputEditText = view.findViewById(R.id.inputPurchasedQuantity)
        private val priceLayout: TextInputLayout = view.findViewById(R.id.layoutQuickPrice)
        private val price: TextInputEditText = view.findViewById(R.id.inputQuickPrice)
        private val unitPrice: TextView = view.findViewById(R.id.textPurchaseUnitPrice)
        private val targetStatus: TextView = view.findViewById(R.id.textPurchaseTargetStatus)
        private var binding = false
        private var boundItem: ShoppingItem? = null
        private var quantityDirty = false
        private var priceDirty = false

        private val quantityWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (!binding) {
                    quantityDirty = true
                    purchasedQuantityLayout.error = null
                    rememberDraft()
                }
            }
        }

        private val priceWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (!binding) {
                    priceDirty = true
                    priceLayout.error = null
                    rememberDraft()
                }
            }
        }

        init {
            purchasedQuantity.addTextChangedListener(quantityWatcher)
            price.addTextChangedListener(priceWatcher)
            purchasedQuantity.setOnFocusChangeListener { _, focused ->
                if (!focused && !binding) commitQuantity()
            }
            price.setOnFocusChangeListener { _, focused ->
                if (!focused && !binding) commitPrice()
            }
            purchasedQuantity.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (commitQuantity()) price.requestFocus()
                    true
                } else false
            }
            price.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (commitEdits()) price.clearFocus()
                    true
                } else false
            }
        }

        private fun rememberDraft() {
            val item = boundItem ?: return
            if (quantityDirty || priceDirty) {
                drafts[item.id] = EditDraft(purchasedQuantity.text?.toString().orEmpty(),
                    price.text?.toString().orEmpty(), quantityDirty, priceDirty)
            } else drafts.remove(item.id)
        }

        fun commitEdits(): Boolean {
            val validQuantity = commitQuantity()
            val validPrice = if (validQuantity) commitPrice() else false
            return validQuantity && validPrice
        }

        private fun commitQuantity(): Boolean {
            if (!quantityDirty) return true
            val item = boundItem ?: return false
            val value = QuickPurchaseInput.parseNonNegative(purchasedQuantity.text?.toString())
            if (value == null) {
                purchasedQuantityLayout.error = "Informe uma quantidade válida"
                return false
            }
            quantityDirty = false
            purchasedQuantityLayout.error = null
            if (item.purchasedQuantity != value) onPurchasedQuantityChanged(item, value)
            if (!priceDirty && item.unitPrice > 0.0 && value > 0.0) {
                binding = true
                price.setText(priceFormat.format(item.unitPrice * value))
                binding = false
            }
            rememberDraft()
            updatePriceSummary(item)
            return true
        }

        private fun commitPrice(): Boolean {
            if (!priceDirty) return true
            val item = boundItem ?: return false
            val total = QuickPurchaseInput.parseNonNegative(price.text?.toString())
            if (total == null) {
                priceLayout.error = "Informe um valor válido"
                return false
            }
            priceDirty = false
            priceLayout.error = null
            onPriceChanged(item, total)
            rememberDraft()
            updatePriceSummary(item)
            return true
        }

        fun bind(item: ShoppingItem) {
            binding = true
            boundItem = item
            quantityDirty = false
            priceDirty = false
            purchasedQuantityLayout.error = null
            priceLayout.error = null
            check.setOnCheckedChangeListener(null)
            check.isChecked = false
            check.contentDescription = "Marcar ${item.name} como comprado"
            name.text = item.name
            quantity.text = "Planejado: ${quantityFormat.format(item.quantity)} ${item.unit}"
            category.text = item.category
            purchasedQuantityLayout.suffixText = item.unit
            priceLayout.hint = "Total pago"

            val actualQuantity = item.purchasedQuantity.takeIf { it > 0.0 } ?: item.quantity
            binding = true
            purchasedQuantity.setText(quantityFormat.format(actualQuantity))
            purchasedQuantity.setSelection(purchasedQuantity.text?.length ?: 0)
            price.setText(if (item.unitPrice > 0.0) priceFormat.format(item.unitPrice * actualQuantity) else "")
            price.setSelection(price.text?.length ?: 0)
            drafts[item.id]?.let { draft ->
                purchasedQuantity.setText(draft.quantity)
                price.setText(draft.total)
                quantityDirty = draft.quantityDirty
                priceDirty = draft.priceDirty
            }
            binding = false
            updatePriceSummary(item, actualQuantity)

            check.setOnCheckedChangeListener { _, checked ->
                if (!binding && checked) {
                    if (commitEdits()) {
                        discardDraft(item.id)
                        onPurchased(item)
                    }
                    else {
                        binding = true
                        check.isChecked = false
                        binding = false
                    }
                }
            }
            itemView.setOnClickListener {
                purchasedQuantity.requestFocus()
                purchasedQuantity.setSelection(purchasedQuantity.text?.length ?: 0)
            }
        }

        private fun updatePriceSummary(item: ShoppingItem, overrideQuantity: Double? = null) {
            val actualQuantity = overrideQuantity
                ?: item.purchasedQuantity.takeIf { it > 0.0 }
                ?: item.quantity
            unitPrice.text = if (item.unitPrice > 0.0) {
                "${PriceUnitHelper.formattedUnitPrice(item, currency)} • total ${currency.format(item.unitPrice * actualQuantity)}"
            } else {
                "Informe quanto comprou e o total pago"
            }
            val product = catalogByName[com.listamercado.app.data.ProductCatalogRepository.normalizeName(item.name)]
            val text = PriceTargetHelper.statusText(item, product, currency)
            targetStatus.text = text.orEmpty()
            targetStatus.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
        }

    }
}
