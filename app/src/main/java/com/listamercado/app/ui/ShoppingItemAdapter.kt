package com.listamercado.app.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.checkbox.MaterialCheckBox
import com.listamercado.app.R
import com.listamercado.app.model.CatalogProduct
import com.listamercado.app.model.ShoppingItem
import com.listamercado.app.util.PriceTargetHelper
import com.listamercado.app.util.PriceUnitHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

class ShoppingItemAdapter(
    private val onChecked: (ShoppingItem, Boolean) -> Unit,
    private val onEdit: (ShoppingItem) -> Unit,
    private val onHistory: (ShoppingItem) -> Unit,
    private val onDelete: (ShoppingItem) -> Unit
) : ListAdapter<ShoppingItemAdapter.Row, ShoppingItemAdapter.Holder>(Diff) {
    data class Row(val item: ShoppingItem, val catalogProduct: CatalogProduct?)
    private val currency = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val quantityFormat = DecimalFormat("0.##", DecimalFormatSymbols(Locale("pt", "BR")))

    fun showItems(newItems: List<ShoppingItem>, catalogProducts: List<CatalogProduct> = emptyList()) {
        val catalog = catalogProducts.associateBy { it.normalizedName }
        submitList(newItems.map { item ->
            Row(item.copy(), catalog[com.listamercado.app.data.ProductCatalogRepository.normalizeName(item.name)]?.copy())
        })
    }

    private object Diff : DiffUtil.ItemCallback<Row>() {
        override fun areItemsTheSame(oldItem: Row, newItem: Row) = oldItem.item.id == newItem.item.id
        override fun areContentsTheSame(oldItem: Row, newItem: Row) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val check: MaterialCheckBox = view.findViewById(R.id.checkPurchased)
        private val name: TextView = view.findViewById(R.id.textName)
        private val meta: TextView = view.findViewById(R.id.textMeta)
        private val note: TextView = view.findViewById(R.id.textNote)
        private val categoryChip: TextView = view.findViewById(R.id.textCategoryChip)
        private val priceTargetStatus: TextView = view.findViewById(R.id.textPriceTargetStatus)
        private val unitPrice: TextView = view.findViewById(R.id.textUnitPrice)
        private val totalPrice: TextView = view.findViewById(R.id.textPrice)
        private val more: ImageButton = view.findViewById(R.id.buttonItemMore)

        fun bind(row: Row) {
            val item = row.item
            check.setOnCheckedChangeListener(null)
            check.isChecked = item.purchased
            name.text = item.name
            meta.text = if (item.purchased) {
                "${quantityFormat.format(item.quantity)} ${item.unit} planejados • ${quantityFormat.format(item.purchasedQuantity)} ${item.unit} comprados"
            } else {
                "${quantityFormat.format(item.quantity)} ${item.unit} planejados"
            }
            categoryChip.text = item.category.ifBlank { "Outros" }
            check.contentDescription = if (item.purchased) {
                "Marcar ${item.name} como pendente"
            } else {
                "Marcar ${item.name} como comprado"
            }

            if (item.unitPrice > 0.0) {
                totalPrice.text = currency.format(if (item.purchased) item.purchasedSubtotal else item.subtotal)
                unitPrice.text = PriceUnitHelper.formattedUnitPrice(item, currency)
            } else {
                totalPrice.text = "Sem preço"
                unitPrice.text = "Toque para informar"
            }

            val targetText = PriceTargetHelper.statusText(item, row.catalogProduct, currency)
            priceTargetStatus.text = targetText.orEmpty()
            priceTargetStatus.visibility = if (targetText.isNullOrBlank()) View.GONE else View.VISIBLE

            note.text = item.note
            note.visibility = if (item.note.isBlank()) View.GONE else View.VISIBLE

            name.paintFlags = if (item.purchased) {
                name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            itemView.alpha = if (item.purchased) 0.68f else 1f

            check.setOnCheckedChangeListener { _, checked -> onChecked(item, checked) }
            itemView.setOnClickListener { onEdit(item) }
            more.setOnClickListener { anchor ->
                PopupMenu(anchor.context, anchor).apply {
                    menu.add("Editar")
                    menu.add("Histórico de preço")
                    menu.add("Excluir")
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.title.toString()) {
                            "Editar" -> onEdit(item)
                            "Histórico de preço" -> onHistory(item)
                            "Excluir" -> onDelete(item)
                        }
                        true
                    }
                    show()
                }
            }
        }
    }
}
