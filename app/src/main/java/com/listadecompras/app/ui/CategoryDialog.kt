package com.listadecompras.app.ui

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.listadecompras.app.R
import com.listadecompras.app.data.CategoryRepository
import com.listadecompras.app.data.PurchaseCategory

object CategoryDialog {
    fun show(context: Context, repository: CategoryRepository, onCreated: (PurchaseCategory) -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_category, null)
        val name = view.findViewById<TextInputEditText>(R.id.inputNewCategoryName)
        val nameLayout = view.findViewById<TextInputLayout>(R.id.layoutNewCategoryName)
        val iconButton = view.findViewById<MaterialButton>(R.id.buttonChooseCategoryIcon)
        var selectedIcon = "🛒"
        iconButton.setOnClickListener {
            chooseIcon(context, selectedIcon) { icon ->
                selectedIcon = icon
                iconButton.text = "$icon  Alterar ícone"
            }
        }
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Nova categoria")
            .setView(view)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Cadastrar", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                nameLayout.error = null
                runCatching { repository.create(name.text?.toString().orEmpty(), selectedIcon) }
                    .onSuccess { onCreated(it); dialog.dismiss() }
                    .onFailure { nameLayout.error = it.message ?: "Não foi possível salvar a categoria." }
            }
        }
        dialog.show()
    }

    /** Ícone selecionado já é persistido na mesma linha SQLite da categoria. */
    fun editIcon(context: Context, repository: CategoryRepository, categoryName: String, onUpdated: (PurchaseCategory) -> Unit) {
        val selected = repository.all().firstOrNull { it.name.equals(categoryName, ignoreCase = true) }
            ?: return
        chooseIcon(context, selected.icon) { icon ->
            runCatching { repository.updateIcon(selected.name, icon) }
                .onSuccess(onUpdated)
                .onFailure { error ->
                    MaterialAlertDialogBuilder(context).setMessage(error.message ?: "Erro ao salvar ícone.")
                        .setPositiveButton("OK", null).show()
                }
        }
    }

    /** Grade rolável de ícones em várias áreas, substituindo a antiga lista vertical. */
    private fun chooseIcon(context: Context, currentIcon: String, onSelected: (String) -> Unit) {
        val density = context.resources.displayMetrics.density
        fun dp(size: Int) = (size * density + .5f).toInt()
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(6), dp(16), dp(8))
        }
        val hint = TextView(context).apply {
            text = "Toque em um símbolo para representar a categoria. Há opções para mercado, tecnologia, carro, bicicleta, casa e outros temas."
            setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant))
            setPadding(dp(4), dp(4), dp(4), dp(12))
        }
        root.addView(hint)
        val scroll = ScrollView(context).apply {
            isFillViewport = true
            overScrollMode = ScrollView.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
        val grid = GridLayout(context).apply {
            columnCount = 5
            alignmentMode = GridLayout.ALIGN_BOUNDS
        }
        val selectedColor = MaterialColors.getColor(root, com.google.android.material.R.attr.colorPrimaryContainer)
        val normalColor = MaterialColors.getColor(root, com.google.android.material.R.attr.colorSurfaceContainerHigh)
        val outlineColor = MaterialColors.getColor(root, com.google.android.material.R.attr.colorPrimary)
        val buttons = mutableListOf<Pair<String, MaterialButton>>()
        var selected = currentIcon
        val columns = grid.columnCount
        CategoryRepository.icons.forEachIndexed { index, emoji ->
            val button = MaterialButton(context).apply {
                text = emoji
                textSize = 24f
                isAllCaps = false
                insetTop = 0; insetBottom = 0
                minWidth = 0; minHeight = dp(52)
                minimumWidth = 0; minimumHeight = dp(52)
                icon = null
                cornerRadius = dp(14)
                setPadding(0, 0, 0, 0)
                contentDescription = "Ícone $emoji"
                setOnClickListener {
                    selected = emoji
                    buttons.forEach { (value, view) ->
                        view.backgroundTintList = ColorStateList.valueOf(if (value == selected) selectedColor else normalColor)
                        view.strokeColor = ColorStateList.valueOf(if (value == selected) outlineColor else normalColor)
                        view.strokeWidth = if (value == selected) dp(2) else 0
                        view.isSelected = value == selected
                    }
                }
            }
            val lp = GridLayout.LayoutParams(GridLayout.spec(index / columns), GridLayout.spec(index % columns, 1f)).apply {
                width = 0; height = dp(56)
                setMargins(dp(3), dp(3), dp(3), dp(3))
            }
            grid.addView(button, lp)
            buttons.add(emoji to button)
        }
        scroll.addView(grid, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(300)))
        buttons.firstOrNull { it.first == selected }?.second?.performClick()
        MaterialAlertDialogBuilder(context)
            .setTitle("Escolha o ícone")
            .setView(root)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Usar ícone") { _, _ -> onSelected(selected) }
            .show()
    }
}
