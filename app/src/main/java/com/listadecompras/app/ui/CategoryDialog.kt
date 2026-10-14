package com.listadecompras.app.ui

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.button.MaterialButton
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
            MaterialAlertDialogBuilder(context)
                .setTitle("Escolha um ícone")
                .setSingleChoiceItems(CategoryRepository.icons.toTypedArray(), CategoryRepository.icons.indexOf(selectedIcon)) { dialog, which ->
                    selectedIcon = CategoryRepository.icons[which]
                    iconButton.text = "$selectedIcon  Alterar ícone"
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
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
                val saved = runCatching { repository.create(name.text?.toString().orEmpty(), selectedIcon) }
                saved.onSuccess {
                    onCreated(it)
                    dialog.dismiss()
                }.onFailure { nameLayout.error = it.message ?: "Não foi possível salvar a categoria." }
            }
        }
        dialog.show()
    }
}
