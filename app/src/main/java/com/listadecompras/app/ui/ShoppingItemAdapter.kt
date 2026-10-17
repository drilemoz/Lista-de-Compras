package com.listadecompras.app.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.checkbox.MaterialCheckBox
import com.listadecompras.app.R
import com.listadecompras.app.data.ProductImages
import com.listadecompras.app.data.CategoryRepository
import com.listadecompras.app.model.CatalogProduct
import com.listadecompras.app.model.ShoppingItem
import com.listadecompras.app.util.PriceTargetHelper
import com.listadecompras.app.util.PriceUnitHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

class ShoppingItemAdapter(
    private val isShopping: Boolean = false,
    private val onChecked: (ShoppingItem, Boolean) -> Unit,
    private val onEdit: (ShoppingItem) -> Unit,
    private val onOpen: (ShoppingItem) -> Unit,
    private val onHistory: (ShoppingItem) -> Unit,
    private val onDelete: (ShoppingItem) -> Unit
) : ListAdapter<ShoppingItemAdapter.Row, ShoppingItemAdapter.Holder>(Diff) {
    data class Row(val item: ShoppingItem, val catalogProduct: CatalogProduct?)
    private val currency = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val quantityFormat = DecimalFormat("0.##", DecimalFormatSymbols(Locale("pt", "BR")))

    fun showItems(newItems: List<ShoppingItem>, catalogProducts: List<CatalogProduct> = emptyList()) {
        val catalog = catalogProducts.associateBy { it.normalizedName }
        submitList(newItems.map { item ->
            Row(item.copy(), catalog[com.listadecompras.app.data.ProductCatalogRepository.normalizeName(item.name)]?.copy())
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
        private val thumbnail: ImageView = view.findViewById(R.id.imageProductThumbnail)
        private val check: MaterialCheckBox = view.findViewById(R.id.checkPurchased)
        private val name: TextView = view.findViewById(R.id.textName)
        private val meta: TextView = view.findViewById(R.id.textMeta)
        private val note: TextView = view.findViewById(R.id.textNote)
        private val categoryChip: TextView = view.findViewById(R.id.textCategoryChip)
        private val savedLink: TextView = view.findViewById(R.id.textSavedLink)
        private val priceTargetStatus: TextView = view.findViewById(R.id.textPriceTargetStatus)
        private val unitPrice: TextView = view.findViewById(R.id.textUnitPrice)
        private val totalPrice: TextView = view.findViewById(R.id.textPrice)
        private val more: ImageButton = view.findViewById(R.id.buttonItemMore)

        fun bind(row: Row) {
            val item = row.item
            check.setOnCheckedChangeListener(null)
            check.isChecked = item.purchased
            name.text = item.name
            val bitmap = ProductImages.decodeThumbnail(itemView.context, item.imageFile)
            thumbnail.setImageBitmap(bitmap)
            thumbnail.visibility = if (bitmap == null) View.GONE else View.VISIBLE
            meta.text = if (item.purchased) {
                "${quantityFormat.format(item.quantity)} ${item.unit} planejados • ${quantityFormat.format(item.purchasedQuantity)} ${item.unit} comprados"
            } else {
                "${quantityFormat.format(item.quantity)} ${item.unit} planejados"
            }
            categoryChip.text = CategoryRepository.label(itemView.context, item.category)
            val showLink = isShopping && item.productUrl.isNotBlank()
            savedLink.visibility = if (showLink) View.VISIBLE else View.GONE
            categoryChip.maxWidth = if (showLink) {
                (124 * itemView.resources.displayMetrics.density + 0.5f).toInt()
            } else Int.MAX_VALUE
            check.contentDescription = if (item.purchased) {
                "Marcar ${item.name} como pendente"
            } else {
                "Marcar ${item.name} como comprado"
            }

            if (item.unitPrice > 0.0 || (isShopping && item.shopFinancedSubtotal > 0.0)) {
                totalPrice.text = currency.format(if (isShopping) {
                    if (item.purchased) item.shopPurchasedSubtotal else item.shopFinancedSubtotal
                } else if (item.purchased) item.purchasedSubtotal else item.subtotal)
                unitPrice.text = if (isShopping) {
                    if (item.installmentCount > 0 && item.installmentAmount > 0.0) {
                        "À vista ${currency.format(item.unitPrice)} • ${item.installmentCount} × ${currency.format(item.installmentAmount)}"
                    } else {
                        "À vista ${currency.format(item.unitPrice)}"
                    }
                } else PriceUnitHelper.formattedUnitPrice(item, currency)
            } else {
                totalPrice.text = "Sem preço"
                unitPrice.text = "Toque para informar"
            }

            val targetText = PriceTargetHelper.statusText(item, row.catalogProduct, currency)
            priceTargetStatus.text = targetText.orEmpty()
            priceTargetStatus.visibility = if (targetText.isNullOrBlank()) View.GONE else View.VISIBLE

            note.text = if (isShopping) listOfNotNull(
                item.storeName.takeIf { it.isNotBlank() },
                item.variant.takeIf { it.isNotBlank() },
                item.note.takeIf { it.isNotBlank() },
                item.shopCalculation?.takeIf { it.extraPercent > 50 }?.let { "⚠ Acréscimo total acima de 50%" }
            ).joinToString(" • ") else item.note
            note.visibility = if (note.text.isBlank()) View.GONE else View.VISIBLE

            name.paintFlags = if (item.purchased) {
                name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            itemView.alpha = if (item.purchased) 0.68f else 1f

            check.setOnCheckedChangeListener { _, checked -> onChecked(item, checked) }
            itemView.setOnClickListener { onOpen(item) }
            more.setOnClickListener { anchor ->
                PopupMenu(anchor.context, anchor).apply {
                    menu.add("Ver detalhes")
                    menu.add("Editar")
                    if (isShopping && item.productUrl.isNotBlank()) menu.add("Abrir link do produto")
                    menu.add("Histórico de preço")
                    menu.add("Excluir")
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.title.toString()) {
                            "Ver detalhes" -> onOpen(item)
                            "Editar" -> onEdit(item)
                            "Abrir link do produto" -> {
                                if (com.listadecompras.app.util.InstallmentCalculator.validUrl(item.productUrl)) {
                                    val url = android.net.Uri.parse(item.productUrl)
                                    runCatching { anchor.context.startActivity(
                                        android.content.Intent(android.content.Intent.ACTION_VIEW, url)) }
                                }
                            }
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
