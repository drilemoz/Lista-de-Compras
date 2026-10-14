package com.listamercado.app.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.chip.Chip
import com.listamercado.app.R
import com.listamercado.app.data.ProductCatalogRepository
import com.listamercado.app.data.ShoppingRepository
import com.listamercado.app.util.InsetsHelper
import com.listamercado.app.model.CatalogProduct
import com.listamercado.app.model.Recurrence
import com.listamercado.app.util.ThemeController

class CatalogActivity : AppCompatActivity() {
    private lateinit var catalogRepository: ProductCatalogRepository
    private lateinit var adapter: CatalogProductAdapter
    private lateinit var search: EditText
    private lateinit var count: TextView
    private lateinit var empty: TextView
    private lateinit var favoritesOnly: Chip
    private lateinit var recurringOnly: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeController.applySavedMode(this)
        setContentView(R.layout.activity_catalog)
        InsetsHelper.applyScaffold(
            activity = this,
            root = findViewById(R.id.rootCatalog),
            scrollable = findViewById(R.id.recyclerCatalog)
        )

        catalogRepository = ProductCatalogRepository(this)
        catalogRepository.seedFromLists(ShoppingRepository(this).loadLists())
        bindViews()
        render()
    }

    private fun bindViews() {
        search = findViewById(R.id.inputCatalogSearch)
        count = findViewById(R.id.textCatalogCount)
        empty = findViewById(R.id.textCatalogEmpty)
        favoritesOnly = findViewById(R.id.chipCatalogFavorites)
        recurringOnly = findViewById(R.id.chipCatalogRecurring)
        favoritesOnly.setOnCheckedChangeListener { _, _ -> render() }
        recurringOnly.setOnCheckedChangeListener { _, _ -> render() }
        adapter = CatalogProductAdapter(
            onFavorite = { product ->
                catalogRepository.setFavorite(product.id, !product.favorite)
                render()
            },
            onRecurring = { product -> chooseRecurrence(product) },
            onPriceTarget = { product -> editPriceTarget(product) }
        )
        findViewById<RecyclerView>(R.id.recyclerCatalog).apply {
            layoutManager = LinearLayoutManager(this@CatalogActivity)
            adapter = this@CatalogActivity.adapter
        }
        findViewById<ImageButton>(R.id.buttonBackCatalog).setOnClickListener { finish() }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = render()
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun editPriceTarget(product: CatalogProduct) {
        PriceTargetDialog.show(this, product) { value ->
            catalogRepository.setPriceTarget(product.id, value, product.unit)
            render()
        }
    }

    private fun chooseRecurrence(product: CatalogProduct) {
        val labels = arrayOf("Não recorrente") + Recurrence.labels()
        val values = arrayOf<String?>(null) + Recurrence.values()
        val checked = values.indexOf(product.recurringFrequency).coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
            .setTitle("Recorrência de ${product.name}")
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                catalogRepository.setRecurringFrequency(product.id, values[which])
                dialog.dismiss()
                render()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun render() {
        val all = catalogRepository.suggestions()
        val query = search.text?.toString().orEmpty()
        val searched = if (query.isBlank()) all else catalogRepository.suggestions(query)
        val visible = searched.filter { product ->
            (!favoritesOnly.isChecked || product.favorite) &&
                (!recurringOnly.isChecked || !product.recurringFrequency.isNullOrBlank())
        }
        adapter.showProducts(visible)
        count.text = "${visible.size} de ${all.size} produtos"
        empty.text = if (all.isEmpty())
            "Nenhum produto no catálogo ainda.\nOs itens das suas listas aparecerão aqui automaticamente."
        else "Nenhum produto encontrado.\nTente limpar a busca ou os filtros."
        empty.visibility = if (visible.isEmpty()) View.VISIBLE else View.GONE
    }
}
