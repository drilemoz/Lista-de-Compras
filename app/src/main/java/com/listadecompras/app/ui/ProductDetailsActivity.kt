package com.listadecompras.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.listadecompras.app.R
import com.listadecompras.app.data.ProductImages
import com.listadecompras.app.data.ShoppingRepository
import com.listadecompras.app.model.ListType
import com.listadecompras.app.model.ShoppingItem
import com.listadecompras.app.util.InstallmentCalculator
import com.listadecompras.app.util.ThemeController
import java.text.NumberFormat
import java.util.Locale

/** Read-only product destination. Editing and photo changes are separate explicit actions. */
class ProductDetailsActivity : AppCompatActivity() {
    private val repo by lazy { ShoppingRepository(this) }
    private val cash = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private var listId = -1L
    private var itemId = -1L
    private lateinit var photos: ProductPhotoCoordinator
    private var barcodeConsumer: ((String) -> Unit)? = null
    private val scanner = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val code = result.data?.getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE)
        if (result.resultCode == RESULT_OK && !code.isNullOrBlank()) barcodeConsumer?.invoke(code)
        barcodeConsumer = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photos = ProductPhotoCoordinator(this, ::savePhoto)
        photos.restoreState(savedInstanceState)
        ThemeController.applySavedMode(this)
        setContentView(R.layout.activity_product_details)
        listId = intent.getLongExtra(EXTRA_LIST_ID, -1)
        itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)
        findViewById<ImageButton>(R.id.buttonBackProduct).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.buttonEditProduct).setOnClickListener {
            val list = repo.loadLists().firstOrNull { it.id == listId } ?: return@setOnClickListener
            val item = list.items.firstOrNull { it.id == itemId } ?: return@setOnClickListener
            ItemDialog.show(this, com.listadecompras.app.data.ProductCatalogRepository(this),
                existing = item, listType = list.listType,
                onRequestBarcode = { callback ->
                    barcodeConsumer = callback
                    scanner.launch(Intent(this, BarcodeScannerActivity::class.java))
                },
                onRequestPhoto = { useCamera, callback -> photos.request(useCamera, callback) }) { edited, barcode, target, unit ->
                val fresh = repo.loadLists()
                val parent = fresh.firstOrNull { it.id == listId } ?: return@show
                val index = parent.items.indexOfFirst { it.id == itemId }
                if (index >= 0) {
                    parent.items[index] = edited.copy(id = itemId)
                    repo.saveLists(fresh)
                    val catalog = com.listadecompras.app.data.ProductCatalogRepository(this)
                    catalog.recordItem(edited, barcode)
                    catalog.setPriceTargetByName(edited.name, target, unit)
                    render()
                }
            }
        }
        findViewById<MaterialButton>(R.id.buttonGalleryProduct).setOnClickListener { photos.request(false, ::savePhoto) }
        findViewById<MaterialButton>(R.id.buttonCameraProduct).setOnClickListener { photos.request(true, ::savePhoto) }
        findViewById<MaterialButton>(R.id.buttonRemovePhoto).setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("Remover foto do produto?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover") { _, _ -> updateItem { it.imageFile = "" }; render() }.show()
        }
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        photos.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun savePhoto(name: String) { updateItem { it.imageFile = name }; render() }

    private fun updateItem(change: (ShoppingItem) -> Unit) {
        val lists = repo.loadLists()
        val item = lists.firstOrNull { it.id == listId }?.items?.firstOrNull { it.id == itemId } ?: return
        change(item)
        repo.saveLists(lists)
    }

    private fun render() {
        val parent = repo.loadLists().firstOrNull { it.id == listId }
        val item = parent?.items?.firstOrNull { it.id == itemId }
        if (parent == null || item == null) { finish(); return }
        findViewById<TextView>(R.id.textProductName).text = item.name
        findViewById<TextView>(R.id.textProductMeta).text =
            "${item.category} • ${item.quantity} ${item.unit} • ${if (item.purchased) "Comprado" else "Pendente"}" +
                item.storeName.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()
        val image = findViewById<ImageView>(R.id.imageProductDetails)
        val bitmap = ProductImages.decode(this, item.imageFile)
        image.setImageBitmap(bitmap)
        image.setOnClickListener {
            if (bitmap != null) MaterialAlertDialogBuilder(this)
                .setView(ImageView(this).apply { setImageBitmap(bitmap); adjustViewBounds = true })
                .setPositiveButton("Fechar", null).show()
        }
        image.visibility = if (bitmap == null) View.INVISIBLE else View.VISIBLE
        findViewById<TextView>(R.id.textImagePlaceholder).visibility = if (bitmap == null) View.VISIBLE else View.GONE
        findViewById<MaterialButton>(R.id.buttonRemovePhoto).visibility = if (bitmap == null) View.GONE else View.VISIBLE
        val shopping = parent.listType == ListType.SHOPPING
        val calc = item.shopCalculation
        findViewById<TextView>(R.id.textProductPayment).text = if (shopping) buildString {
            append("À vista: ${cash.format(item.unitPrice)}")
            append("\nTotal à vista com frete: ${cash.format(item.shopCashSubtotal)}")
            append("\nTotal financiado (quantidade): ${cash.format(item.shopFinancedSubtotal)}")
            if (item.installmentCount > 0) {
                append("\n${item.installmentCount} × ${cash.format(item.installmentAmount)} por unidade")
                append("\nEntrada: ${cash.format(item.downPayment)} • Frete: ${cash.format(item.shipping)} • Taxas: ${cash.format(item.extraFees)}")
                append("\nPagas: ${item.paidInstallments} de ${item.installmentCount}")
                InstallmentCalculator.nextDueDate(item.firstDueDate, item.paidInstallments,
                    item.installmentCount)?.let { append("\nPróximo vencimento: $it") }
                if (calc != null) append("\nAcréscimo total: ${cash.format(calc.extraCost)} (${String.format(Locale("pt", "BR"), "%.1f", calc.extraPercent)}%)")
            }
            if (item.alternativeStore.isNotBlank() || item.alternativeCashPrice != null) {
                val alt = if (item.alternativeInstallments > 0)
                    item.alternativeDownPayment + item.alternativeInstallments * item.alternativeInstallmentAmount + item.alternativeShipping + item.alternativeFees
                else (item.alternativeCashPrice ?: 0.0) + item.alternativeShipping + item.alternativeFees
                append("\n\nOutra oferta: ${item.alternativeStore.ifBlank { "Loja alternativa" }}")
                append("\nTotal por unidade: ${cash.format(alt)}")
                append("\nDiferença por unidade: ${cash.format(alt - (calc?.financedTotal ?: item.unitPrice))}")
            }
        } else "Preço unitário: ${cash.format(item.unitPrice)}\nSubtotal: ${cash.format(item.subtotal)}"
        findViewById<TextView>(R.id.textProductWarning).apply {
            visibility = if (shopping && (calc?.extraPercent ?: 0.0) > 50.0) View.VISIBLE else View.GONE
            text = "⚠ Acréscimo total acima de 50%. Não é uma taxa de juros mensal."
        }
        findViewById<TextView>(R.id.textProductNotes).apply {
            text = listOfNotNull(item.variant.takeIf { it.isNotBlank() }?.let { "Variação: $it" },
                item.note.takeIf { it.isNotBlank() }?.let { "Observações: $it" }).joinToString("\n")
            visibility = if (text.isBlank()) View.GONE else View.VISIBLE
        }
        findViewById<View>(R.id.cardProductLinks).visibility =
            if (shopping && (item.productUrl.isNotBlank() || item.alternativeUrl.isNotBlank())) View.VISIBLE else View.GONE
        for (id in listOf(R.id.buttonOpenProductUrl, R.id.buttonCopyProductUrl, R.id.buttonShareProductUrl)) {
            findViewById<View>(id).visibility = if (item.productUrl.isNotBlank()) View.VISIBLE else View.GONE
        }
        for (id in listOf(R.id.buttonOpenAlternativeUrl, R.id.buttonCopyAlternativeUrl, R.id.buttonShareAlternativeUrl)) {
            findViewById<View>(id).visibility = if (item.alternativeUrl.isNotBlank()) View.VISIBLE else View.GONE
        }
        findViewById<MaterialButton>(R.id.buttonOpenProductUrl).setOnClickListener { openLink(item.productUrl) }
        findViewById<MaterialButton>(R.id.buttonOpenAlternativeUrl).setOnClickListener { openLink(item.alternativeUrl) }
        findViewById<MaterialButton>(R.id.buttonCopyProductUrl).setOnClickListener { copyUrl(item.productUrl) }
        findViewById<MaterialButton>(R.id.buttonCopyAlternativeUrl).setOnClickListener { copyUrl(item.alternativeUrl) }
        findViewById<MaterialButton>(R.id.buttonShareProductUrl).setOnClickListener { shareUrl(item.productUrl) }
        findViewById<MaterialButton>(R.id.buttonShareAlternativeUrl).setOnClickListener { shareUrl(item.alternativeUrl) }
    }

    private fun copyUrl(link: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Link do produto", link))
        toast("Link copiado")
    }
    private fun shareUrl(link: String) {
        runCatching { startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, link)
        }, "Compartilhar anúncio")) }.onFailure { toast("Não foi possível compartilhar o anúncio") }
    }

    private fun openLink(link: String) {
        if (!InstallmentCalculator.validUrl(link) || link.isBlank()) { toast("Link inválido"); return }
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }
            .onFailure { toast("Nenhum aplicativo disponível para abrir o link") }
    }
    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_LIST_ID = "list_id"
        const val EXTRA_ITEM_ID = "item_id"
    }
}

