package com.listadecompras.app.ui
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.listadecompras.app.util.PriceSplitHelper
import java.text.NumberFormat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.listadecompras.app.R
import com.listadecompras.app.data.BarcodeLookupRepository
import com.listadecompras.app.data.CategoryRepository
import com.listadecompras.app.data.PurchaseCategory
import com.listadecompras.app.data.ProductImages
import com.listadecompras.app.data.ProductCatalogRepository
import com.listadecompras.app.model.BarcodeLookupResult
import com.listadecompras.app.model.CatalogProduct
import com.listadecompras.app.model.ShoppingItem
import com.listadecompras.app.model.ListType
import com.listadecompras.app.util.CurrencyTextWatcher
import com.listadecompras.app.util.InsetsHelper
import com.listadecompras.app.util.PriceUnitHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
object ItemDialog {
    private const val DEFAULT_CATEGORY = "Outros"
    private val units = listOf("un", "kg", "g", "L", "mL", "pct", "pacote", "lata", "cx")
    fun show(
        context: Context,
        catalogRepository: ProductCatalogRepository,
        existing: ShoppingItem? = null,
        listType: ListType = ListType.SUPERMARKET,
        initialBarcode: String? = null,
        onRequestBarcode: (((String) -> Unit) -> Unit)? = null,
        onRequestPhoto: ((Boolean, (String) -> Unit) -> Unit)? = null,
        onSave: (ShoppingItem, String?, Double?, String) -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_item, null)
        val lookupRepository = BarcodeLookupRepository(context)
        val sheet = view.findViewById<View>(R.id.sheetItemRoot)
        val title = view.findViewById<TextView>(R.id.textSheetTitle)
        val subtitle = view.findViewById<TextView>(R.id.textSheetSubtitle)
        val nameHelper = view.findViewById<TextView>(R.id.textNameHelper)
        val barcodeStatus = view.findViewById<TextView>(R.id.textBarcodeStatus)
        val lookupStatus = view.findViewById<TextView>(R.id.textLookupStatus)
        val lookupProgress = view.findViewById<LinearProgressIndicator>(R.id.progressLookup)
        val nameLayout = view.findViewById<TextInputLayout>(R.id.layoutName)
        val quantityLayout = view.findViewById<TextInputLayout>(R.id.layoutQuantity)
        val purchasedQuantityLayout = view.findViewById<TextInputLayout>(R.id.layoutPurchasedQuantity)
        val priceLayout = view.findViewById<TextInputLayout>(R.id.layoutPrice)
        val targetLayout = view.findViewById<TextInputLayout>(R.id.layoutPriceTarget)
        val photoPreview = view.findViewById<android.widget.ImageView>(R.id.imageItemPhoto)
        var selectedImage = existing?.imageFile.orEmpty()
        fun updatePhotoPreview() {
            val bmp = ProductImages.decode(context, selectedImage)
            photoPreview.setImageBitmap(bmp)
            if (bmp == null) {
                photoPreview.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }
        updatePhotoPreview()
        view.findViewById<MaterialButton>(R.id.buttonItemGallery).setOnClickListener {
            onRequestPhoto?.invoke(false) { image -> selectedImage = image; updatePhotoPreview() }
        }
        view.findViewById<MaterialButton>(R.id.buttonItemCamera).setOnClickListener {
            onRequestPhoto?.invoke(true) { image -> selectedImage = image; updatePhotoPreview() }
        }
        view.findViewById<MaterialButton>(R.id.buttonItemRemovePhoto).setOnClickListener {
            selectedImage = ""; updatePhotoPreview()
        }
        val name = view.findViewById<MaterialAutoCompleteTextView>(R.id.inputName)
        val quantity = view.findViewById<TextInputEditText>(R.id.inputQuantity)
        val purchasedQuantity = view.findViewById<TextInputEditText>(R.id.inputPurchasedQuantity)
        val price = view.findViewById<TextInputEditText>(R.id.inputPrice)
        val targetPrice = view.findViewById<TextInputEditText>(R.id.inputPriceTarget)
        val note = view.findViewById<TextInputEditText>(R.id.inputNote)
        val category = view.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerCategory)
        val newCategoryButton = view.findViewById<MaterialButton>(R.id.buttonNewCategory)
        val selectedCategoryIcon = view.findViewById<TextView>(R.id.textSelectedCategoryIcon)
        val checkSplit = view.findViewById<MaterialCheckBox>(R.id.checkSplitPrice)
        val comboCard = view.findViewById<View>(R.id.cardSplitPrice)
        val comboTotalLayout = view.findViewById<TextInputLayout>(R.id.layoutComboTotal)
        val comboCountLayout = view.findViewById<TextInputLayout>(R.id.layoutComboUnits)
        val comboTotal = view.findViewById<TextInputEditText>(R.id.inputComboTotal)
        val comboCount = view.findViewById<TextInputEditText>(R.id.inputComboUnits)
        val comboPreview = view.findViewById<TextView>(R.id.textComboPreview)
        val priceWatcher = CurrencyTextWatcher(price)
        val targetWatcher = CurrencyTextWatcher(targetPrice)
        price.addTextChangedListener(priceWatcher)
        targetPrice.addTextChangedListener(targetWatcher)
        val brazilCurrency = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val shopping = listType == ListType.SHOPPING
        val shopForm = ShoppingFieldsForm(view, priceWatcher, brazilCurrency)
        shopForm.section.visibility = if (shopping) View.VISIBLE else View.GONE
        if (shopping) {
            priceLayout.hint = "Preço à vista por unidade"
        }
        price.doAfterTextChanged { if (shopping) shopForm.update() }
        val moreButton = view.findViewById<MaterialButton>(R.id.buttonMoreOptions)
        val moreContainer = view.findViewById<View>(R.id.containerMoreOptions)
        moreButton.setOnClickListener {
            moreContainer.visibility = if (moreContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            moreButton.text = if (moreContainer.visibility == View.VISIBLE) "Ocultar opções" else "Mais opções: preço-alvo e observação"
        }
        val comboWatcher = CurrencyTextWatcher(comboTotal)
        comboTotal.addTextChangedListener(comboWatcher)
        val categoryRepository = CategoryRepository(context)
        var categories = categoryRepository.all()
        fun refreshCategories(selectedName: String? = null) {
            categories = categoryRepository.all()
            val adapter = object : ArrayAdapter<PurchaseCategory>(context, android.R.layout.simple_dropdown_item_1line, categories) {
                override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                    val row = super.getView(position, convertView, parent) as TextView
                    row.text = "${getItem(position)?.icon}   ${getItem(position)?.name}"
                    row.setPadding(24, 18, 16, 18)
                    return row
                }
                override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View =
                    getView(position, convertView, parent)
            }
            category.setAdapter(adapter)
            category.setOnItemClickListener { _, _, index, _ ->
                categories.getOrNull(index)?.let { category.setText(it.name, false) }
            }
            if (selectedName != null) category.setText(selectedName, false)
        }
        val unit = view.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerUnit)
        val buttonScan = view.findViewById<MaterialButton>(R.id.buttonScanBarcode)
        val buttonCancel = view.findViewById<MaterialButton>(R.id.buttonCancel)
        val buttonSave = view.findViewById<MaterialButton>(R.id.buttonSave)
        val buttonClose = view.findViewById<android.widget.ImageButton>(R.id.buttonCloseSheet)
        val knownProducts = catalogRepository.suggestions()
        val productByDisplayedName = knownProducts.associateBy { it.name }
        name.setAdapter(
            ArrayAdapter(
                context,
                android.R.layout.simple_dropdown_item_1line,
                knownProducts.map { it.name }
            )
        )
        title.text = if (shopping) (if (existing == null) "Adicionar compra" else "Editar compra")
            else (if (existing == null) "Adicionar produto" else "Editar produto")
        subtitle.text = if (existing == null) {
            "Pesquise no catálogo ou adicione um produto."
        } else {
            "Altere os dados sem perder o histórico da lista."
        }
        refreshCategories(DEFAULT_CATEGORY)
        category.doAfterTextChanged {
            val selected = category.text?.toString()?.trim().orEmpty()
            selectedCategoryIcon.text = categories.firstOrNull { it.name.equals(selected, ignoreCase = true) }?.icon ?: "🛒"
        }
        newCategoryButton.setOnClickListener {
            CategoryDialog.show(context, categoryRepository) { created ->
                refreshCategories(created.name)
            }
        }
        selectedCategoryIcon.setOnClickListener {
            val current = category.text?.toString()?.trim().orEmpty()
            CategoryDialog.editIcon(context, categoryRepository, current) { edited ->
                refreshCategories(edited.name)
            }
        }
        unit.setSimpleItems(if (shopping) arrayOf("un", "par", "kit", "peça", "cx", "pct") else units.toTypedArray())
        category.setText(DEFAULT_CATEGORY, false)
        unit.setText(units.first(), false)
        var selectedBarcode: String? = existing
            ?.let { catalogRepository.findByName(it.name)?.barcode }
        var scannedUnknownBarcode: String? = null
        var dialogOpen = true
        var pendingLookupBarcode: String? = null
        var suppressNameWatcher = false
        var statusMessage: String? = null
        fun updateSplitPreview() {
            if (!checkSplit.isChecked) return
            val total = comboWatcher.amount()
            val count = comboCount.text?.toString()?.toIntOrNull() ?: 0
            if (PriceSplitHelper.isValid(total, count)) {
                comboPreview.text = "${brazilCurrency.format(PriceSplitHelper.roundedUnitPrice(total, count))} por ${PriceUnitHelper.priceUnit(unit.text?.toString().orEmpty().ifBlank { "un" })}"
                priceWatcher.setAmount(PriceSplitHelper.roundedUnitPrice(total, count))
                comboTotalLayout.error = null
                comboCountLayout.error = null
            } else {
                comboPreview.text = "Informe o total e entre 2 e 999 unidades."
                priceWatcher.setAmount(0.0)
            }
        }
        var manualPriceBeforeSplit = 0.0
        checkSplit.setOnCheckedChangeListener { _, enabled ->
            if (enabled) manualPriceBeforeSplit = priceWatcher.amount()
            comboCard.visibility = if (enabled) View.VISIBLE else View.GONE
            price.isEnabled = !enabled
            priceLayout.hint = if (enabled) "Preço unitário calculado" else PriceUnitHelper.inputHint(unit.text?.toString().orEmpty())
            if (enabled) updateSplitPreview()
            else {
                priceWatcher.setAmount(manualPriceBeforeSplit)
            }
        }
        comboTotal.doAfterTextChanged { updateSplitPreview() }
        comboCount.doAfterTextChanged { updateSplitPreview() }
        fun setLookupStatus(message: String?, visibleWhenEmpty: Boolean = false) {
            statusMessage = message
            lookupStatus.text = message.orEmpty()
            lookupStatus.visibility = if (message.isNullOrBlank() && !visibleWhenEmpty) View.GONE else View.VISIBLE
        }
        fun setLookupLoading(isLoading: Boolean) {
            lookupProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                setLookupStatus("Consultando nome do produto...", visibleWhenEmpty = true)
            }
        }
        fun renderBarcodeStatus(isNew: Boolean = false, fromCatalog: Boolean = false) {
            val code = selectedBarcode.orEmpty()
            barcodeStatus.text = when {
                code.isBlank() -> "Sem código associado"
                fromCatalog -> "Código $code • catálogo local"
                isNew -> "Código $code • novo produto"
                else -> "Código associado • $code"
            }
        }
        fun applyCatalogProduct(product: CatalogProduct) {
            pendingLookupBarcode = null
            setLookupLoading(false)
            suppressNameWatcher = true
            name.setText(product.name, false)
            suppressNameWatcher = false
            category.setText(product.category.ifBlank { DEFAULT_CATEGORY }, false)
            unit.setText(if (shopping) "un" else product.unit.ifBlank { units.first() }, false)
            priceLayout.hint = PriceUnitHelper.inputHint(product.unit)
            if (checkSplit.isChecked) {
                checkSplit.isChecked = false
                comboWatcher.setAmount(0.0)
                comboCount.setText("")
            }
            if (product.lastUnitPrice > 0.0) {
                if (!shopping || product.unit !in listOf("kg", "g", "L", "mL")) {
                    priceWatcher.setAmount(PriceUnitHelper.editorAmount(product.lastUnitPrice, product.unit))
                }
            }
            targetWatcher.setAmount(product.priceTarget ?: 0.0)
            targetLayout.hint = "Preço-alvo por ${product.priceTargetUnit ?: PriceUnitHelper.priceUnit(product.unit)}"
            selectedBarcode = scannedUnknownBarcode ?: product.barcode
            renderBarcodeStatus(isNew = scannedUnknownBarcode != null, fromCatalog = scannedUnknownBarcode == null)
            nameHelper.text = "Produto conhecido. Toque numa sugestão para preencher automaticamente."
            setLookupStatus("Preenchido pelo catálogo local.")
        }

        fun applyOnlineLookup(result: BarcodeLookupResult) {
            val currentName = name.text?.toString()?.trim().orEmpty()
            if (currentName.isBlank()) {
                suppressNameWatcher = true
                name.setText(result.name, false)
                suppressNameWatcher = false
            }
            val currentCategory = category.text?.toString()?.trim().orEmpty()
            if ((currentCategory.isBlank() || currentCategory == DEFAULT_CATEGORY || currentCategory == "Outros")
                && !result.category.isNullOrBlank()
            ) {
                category.setText(result.category, false)
            }
            nameHelper.text = "Dados externos são opcionais: revise o nome antes de salvar."
            val extras = buildList {
                result.brand?.takeIf { it.isNotBlank() }?.let { add(it) }
                result.quantityDescription?.takeIf { it.isNotBlank() }?.let { add(it) }
            }.joinToString(" • ")
            val suffix = if (extras.isNotBlank()) ": $extras" else ""
            setLookupStatus("Encontrado online em ${result.source}. Revise antes de salvar$suffix")
        }

        fun lookupBarcodeOnline(barcode: String) {
            pendingLookupBarcode = barcode
            setLookupLoading(true)
            lookupRepository.lookup(barcode) { result ->
                if (!dialogOpen || pendingLookupBarcode != barcode) return@lookup
                setLookupLoading(false)
                result
                    .onSuccess { lookup ->
                        if (lookup == null) {
                            setLookupStatus("Código $barcode lido. Nome não encontrado automaticamente; preencha o produto e salve para reconhecê-lo nas próximas vezes.")
                        } else {
                            applyOnlineLookup(lookup)
                        }
                    }
                    .onFailure {
                        setLookupStatus("Código $barcode lido. A consulta online falhou, mas você pode preencher e salvar normalmente.")
                    }
            }
        }

        existing?.let {
            suppressNameWatcher = true
            name.setText(it.name, false)
            suppressNameWatcher = false
            quantity.setText(it.quantity.toInput())
            if (it.purchased || it.purchasedQuantity > 0.0) {
                purchasedQuantityLayout.visibility = View.VISIBLE
                purchasedQuantity.setText((it.purchasedQuantity.takeIf { value -> value > 0.0 } ?: it.quantity).toInput())
            }
            priceWatcher.setAmount(PriceUnitHelper.editorAmount(it.unitPrice, it.unit))
            if (it.comboTotalPrice != null && it.comboUnitCount != null &&
                PriceSplitHelper.isValid(it.comboTotalPrice!!, it.comboUnitCount!!)) {
                comboWatcher.setAmount(it.comboTotalPrice!!)
                comboCount.setText(it.comboUnitCount!!.toString())
                checkSplit.isChecked = true
            }
            if (shopping) shopForm.restore(it)
            note.setText(it.note)
            if (it.note.isNotBlank()) moreContainer.visibility = View.VISIBLE
            category.setText(it.category, false)
            unit.setText(if (shopping && it.unit in listOf("kg", "g", "L", "mL")) "un" else it.unit, false)
            if (!checkSplit.isChecked) priceLayout.hint = PriceUnitHelper.inputHint(it.unit)
            catalogRepository.findByName(it.name)?.let { product ->
                targetWatcher.setAmount(product.priceTarget ?: 0.0)
                if (product.priceTarget != null) moreContainer.visibility = View.VISIBLE
                targetLayout.hint = "Preço-alvo por ${product.priceTargetUnit ?: PriceUnitHelper.priceUnit(it.unit)}"
            }
        } ?: run {
            quantity.setText("1")
            quantity.setSelection(quantity.text?.length ?: 0)
            priceLayout.hint = PriceUnitHelper.inputHint(units.first())
            targetLayout.hint = "Preço-alvo por ${PriceUnitHelper.priceUnit(units.first())}"
        }
        if (moreContainer.visibility == View.VISIBLE) moreButton.text = "Ocultar opções"
        if (shopping) shopForm.update()
        renderBarcodeStatus()

        name.doAfterTextChanged {
            if (!suppressNameWatcher) {
                nameLayout.error = null
                if (pendingLookupBarcode != null && statusMessage?.contains("Consultando") == true) {
                    setLookupStatus("Lendo código... você pode ajustar o nome manualmente se quiser.", visibleWhenEmpty = true)
                }
            }
        }

        name.setOnItemClickListener { _, _, position, _ ->
            val selectedName = name.adapter.getItem(position)?.toString().orEmpty()
            val product = productByDisplayedName[selectedName] ?: catalogRepository.findByName(selectedName)
            if (product != null) applyCatalogProduct(product)
        }

        name.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                catalogRepository.findByName(name.text?.toString().orEmpty())?.let { product ->
                    if (existing == null) applyCatalogProduct(product)
                }
            }
        }

        unit.setOnItemClickListener { _, _, _, _ ->
            val unitValue = unit.text?.toString().orEmpty()
            if (!checkSplit.isChecked) priceLayout.hint = PriceUnitHelper.inputHint(unitValue)
            else updateSplitPreview()
            targetLayout.hint = "Preço-alvo por ${PriceUnitHelper.priceUnit(unitValue)}"
        }

        fun handleBarcode(rawBarcode: String) {
            val barcode = ProductCatalogRepository.sanitizeBarcode(rawBarcode)
            if (barcode == null) {
                setLookupStatus("A leitura não retornou um código válido. Tente novamente.", visibleWhenEmpty = true)
                return
            }
            val product = catalogRepository.findByBarcode(barcode)
            selectedBarcode = barcode
            if (product != null) {
                scannedUnknownBarcode = null
                pendingLookupBarcode = null
                applyCatalogProduct(product)
            } else {
                scannedUnknownBarcode = barcode
                renderBarcodeStatus(isNew = true)
                setLookupStatus("Código $barcode reconhecido. Tentando identificar o produto online...", visibleWhenEmpty = true)
                lookupBarcodeOnline(barcode)
            }
        }

        buttonScan.isEnabled = onRequestBarcode != null
        buttonScan.alpha = if (buttonScan.isEnabled) 1f else 0.5f
        buttonScan.setOnClickListener {
            val request = onRequestBarcode ?: return@setOnClickListener
            request { rawBarcode -> handleBarcode(rawBarcode) }
        }

        val dialog = BottomSheetDialog(context)
        dialog.setContentView(view)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        dialog.behavior.isDraggable = true
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        InsetsHelper.applyBottomSheetInsets(sheet)
        dialog.setOnDismissListener {
            dialogOpen = false
            categoryRepository.close()
        }

        buttonClose.setOnClickListener { dialog.dismiss() }
        buttonCancel.setOnClickListener { dialog.dismiss() }
        buttonSave.setOnClickListener {
            nameLayout.error = null
            quantityLayout.error = null
            purchasedQuantityLayout.error = null
            priceLayout.error = null
            targetLayout.error = null
            comboTotalLayout.error = null
            comboCountLayout.error = null

            val comboValue = comboWatcher.amount()
            val comboNumber = comboCount.text?.toString()?.toIntOrNull()
            if (checkSplit.isChecked && (comboNumber == null || !PriceSplitHelper.isValid(comboValue, comboNumber))) {
                if (comboValue <= 0.0) comboTotalLayout.error = "Informe um total positivo"
                if (comboNumber == null || comboNumber !in 2..999) comboCountLayout.error = "Use de 2 a 999 unidades"
                return@setOnClickListener
            }

            if (shopping && !shopForm.validate()) return@setOnClickListener

            val itemName = name.text?.toString()?.trim().orEmpty()
            if (itemName.isBlank()) {
                nameLayout.error = "Digite o nome do item"
                name.requestFocus()
                return@setOnClickListener
            }

            val parsedQuantity = quantity.text?.toString()?.replace(',', '.')?.toDoubleOrNull()
            if (parsedQuantity == null || parsedQuantity <= 0.0) {
                quantityLayout.error = "Informe uma quantidade válida"
                quantity.requestFocus()
                return@setOnClickListener
            }

            val unitValue = unit.text?.toString()?.ifBlank { units.first() } ?: units.first()
            val categoryValue = category.text?.toString()?.trim()?.ifBlank { DEFAULT_CATEGORY } ?: DEFAULT_CATEGORY
            // Categorias vindas do código de barras ou do catálogo também entram no banco.
            if (categories.none { it.name.equals(categoryValue, ignoreCase = true) }) {
                runCatching { categoryRepository.create(categoryValue, "🛒") }
            }
            val parsedPurchasedQuantity = if (purchasedQuantityLayout.visibility == View.VISIBLE) {
                purchasedQuantity.text?.toString()?.replace(',', '.')?.toDoubleOrNull()
            } else {
                existing?.purchasedQuantity ?: 0.0
            }
            if (existing?.purchased == true && (parsedPurchasedQuantity == null || parsedPurchasedQuantity <= 0.0)) {
                purchasedQuantityLayout.error = "Informe quanto foi realmente comprado"
                purchasedQuantity.requestFocus()
                return@setOnClickListener
            }

            val item = (existing?.copy() ?: ShoppingItem(name = itemName)).apply {
                this.name = itemName
                this.quantity = parsedQuantity
                this.purchasedQuantity = parsedPurchasedQuantity?.coerceAtLeast(0.0) ?: 0.0
                this.unitPrice = PriceUnitHelper.internalAmount(
                    if (checkSplit.isChecked) PriceSplitHelper.unitPrice(comboValue, comboNumber!!)
                    else priceWatcher.amount().coerceAtLeast(0.0),
                    unitValue
                )
                this.comboTotalPrice = if (checkSplit.isChecked) comboValue else null
                this.comboUnitCount = if (checkSplit.isChecked) comboNumber else null
                this.category = categoryValue
                this.unit = unitValue
                this.note = note.text?.toString()?.trim().orEmpty()
                this.imageFile = selectedImage
                if (shopping) shopForm.applyTo(this)
            }
            onSave(
                item,
                selectedBarcode,
                targetWatcher.amount().takeIf { it > 0.0 },
                unitValue
            )
            dialog.dismiss()
        }
        dialog.show()
        initialBarcode?.let { barcode ->
            view.post { handleBarcode(barcode) }
        }
    }

    private fun Double.toInput(): String {
        val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
        return DecimalFormat("0.##", symbols).format(this)
    }
}
