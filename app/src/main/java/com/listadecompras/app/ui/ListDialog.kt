package com.listadecompras.app.ui

import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.listadecompras.app.R
import com.listadecompras.app.model.ListType
import com.listadecompras.app.util.InsetsHelper

object ListDialog {
    fun show(
        context: AppCompatActivity,
        initialName: String = "",
        titleOverride: String? = null,
        subtitleOverride: String? = null,
        actionLabel: String? = null,
        listType: ListType = ListType.SUPERMARKET,
        onSave: (String) -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        val sheet = view.findViewById<android.view.View>(R.id.sheetListRoot)
        val title = view.findViewById<android.widget.TextView>(R.id.textListSheetTitle)
        val subtitle = view.findViewById<android.widget.TextView>(R.id.textListSheetSubtitle)
        val inputLayout = view.findViewById<TextInputLayout>(R.id.layoutListName)
        val name = view.findViewById<TextInputEditText>(R.id.inputListName)
        val buttonSave = view.findViewById<MaterialButton>(R.id.buttonSaveList)
        val buttonCancel = view.findViewById<MaterialButton>(R.id.buttonCancelList)
        val buttonClose = view.findViewById<ImageButton>(R.id.buttonCloseListSheet)

        title.text = titleOverride ?: when {
            initialName.isNotBlank() -> "Renomear lista"
            listType == ListType.SHOPPING -> "Nova lista de Shopping"
            else -> "Nova lista"
        }
        subtitle.text = subtitleOverride ?: if (initialName.isBlank()) {
            if (listType == ListType.SHOPPING) "Organize lojas, links, preços à vista e parcelas." else "Crie uma lista para cada mercado, data de compra ou ocasião."
        } else {
            "Dê um nome mais claro para identificar seu mercado ou compra."
        }

        val creating = initialName.isBlank()
        view.findViewById<android.view.View>(R.id.layoutListSuggestions).visibility =
            if (creating) android.view.View.VISIBLE else android.view.View.GONE
        buttonSave.text = actionLabel ?: if (creating) "Criar lista" else "Salvar alterações"
        val choices = if (listType == ListType.SHOPPING) listOf(
            Triple(R.id.buttonSuggestionWeek, "📱 Tecnologia", "Tecnologia"),
            Triple(R.id.buttonSuggestionFair, "👕 Roupas", "Roupas e calçados"),
            Triple(R.id.buttonSuggestionPharmacy, "🚲 Bicicleta", "Acessórios para bicicleta"),
            Triple(R.id.buttonSuggestionTrip, "🏠 Casa", "Itens para casa")
        ) else listOf(
            Triple(R.id.buttonSuggestionWeek, "🛒 Semana", "Compras da semana"),
            Triple(R.id.buttonSuggestionFair, "🥕 Feira", "Feira"),
            Triple(R.id.buttonSuggestionPharmacy, "💊 Farmácia", "Farmácia"),
            Triple(R.id.buttonSuggestionTrip, "🚲 Viagem", "Viagem")
        )
        choices.forEach { (id, caption, label) ->
            view.findViewById<MaterialButton>(id).apply {
                text = caption
                setOnClickListener {
                name.setText(label)
                name.setSelection(label.length)
                }
            }
        }
        name.setText(initialName)
        if (initialName.isNotBlank()) name.setSelection(initialName.length)

        val dialog = BottomSheetDialog(context)
        dialog.setContentView(view)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        InsetsHelper.applyBottomSheetInsets(sheet)

        buttonClose.setOnClickListener { dialog.dismiss() }
        buttonCancel.setOnClickListener { dialog.dismiss() }
        buttonSave.setOnClickListener {
            inputLayout.error = null
            val value = name.text?.toString()?.trim().orEmpty()
            if (value.isBlank()) {
                inputLayout.error = "Digite um nome para a lista"
                name.requestFocus()
                return@setOnClickListener
            }
            onSave(value)
            dialog.dismiss()
        }
        dialog.show()
    }
}
