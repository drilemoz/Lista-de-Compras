package com.listadecompras.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.listadecompras.app.R
import com.listadecompras.app.model.ShoppingList
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class ShoppingListAdapter(
    private val onOpen: (ShoppingList) -> Unit,
    private val onMore: (ShoppingList) -> Unit
) : ListAdapter<ShoppingList, ShoppingListAdapter.Holder>(Diff) {
    private val currency = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val date = DateFormat.getDateInstance(DateFormat.SHORT, Locale("pt", "BR"))

    fun showLists(newLists: List<ShoppingList>) {
        submitList(newLists.map { list ->
            list.copy(items = list.items.map { it.copy() }.toMutableList())
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    private object Diff : DiffUtil.ItemCallback<ShoppingList>() {
        override fun areItemsTheSame(oldItem: ShoppingList, newItem: ShoppingList) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList) = oldItem == newItem
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val emoji: TextView = view.findViewById(R.id.textListEmoji)
        private val name: TextView = view.findViewById(R.id.textListName)
        private val meta: TextView = view.findViewById(R.id.textListMeta)
        private val stateChip: Chip = view.findViewById(R.id.chipListState)
        private val marketChip: Chip = view.findViewById(R.id.chipListMarket)
        private val progressLabel: TextView = view.findViewById(R.id.textListProgress)
        private val total: TextView = view.findViewById(R.id.textListTotal)
        private val progress: LinearProgressIndicator = view.findViewById(R.id.progressList)
        private val hint: TextView = view.findViewById(R.id.textListHint)
        private val more: ImageButton = view.findViewById(R.id.buttonListMore)

        fun bind(list: ShoppingList) {
            name.text = list.name
            emoji.text = pickEmoji(list)
            val count = list.items.size
            meta.text = "Atualizada ${date.format(Date(list.updatedAt))} • $count ${if (count == 1) "item" else "itens"}"

            val purchaseLabel = when {
                count == 0 -> "Lista vazia"
                list.purchasedCount == 0 -> "Nada comprado"
                list.purchasedCount == count -> "Compra concluída"
                else -> "${list.purchasedCount} de $count comprados"
            }
            stateChip.text = when {
                count == 0 -> "Nova"
                list.purchasedCount == 0 -> "A fazer"
                list.purchasedCount == count -> "Concluída"
                else -> "Em andamento"
            }

            val percent = if (count == 0) 0 else ((list.purchasedCount * 100.0) / count).toInt()
            progress.setProgressCompat(percent, false)
            progressLabel.text = if (count == 0) {
                "Adicione itens e preços para começar"
            } else {
                "$purchaseLabel • $percent%"
            }
            total.text = if (list.estimatedTotal > 0.0) currency.format(list.estimatedTotal) else "Sem preços"

            val marketName = list.marketName.orEmpty().trim()
            marketChip.visibility = if (marketName.isBlank()) View.GONE else View.VISIBLE
            if (marketName.isNotBlank()) {
                marketChip.text = marketName
            }

            hint.text = when {
                count == 0 -> "Toque para adicionar os primeiros produtos"
                list.budget > 0.0 && list.estimatedTotal > list.budget -> "Acima do orçamento planejado"
                list.budget > 0.0 && list.estimatedTotal > 0.0 -> "Orçamento ${currency.format(list.budget)} • dentro do planejado"
                list.purchasedCount == count && count > 0 -> "Tudo comprado • toque para revisar"
                else -> "Toque para abrir e continuar a compra"
            }

            itemView.setOnClickListener { onOpen(list) }
            more.setOnClickListener { onMore(list) }
        }

        private fun pickEmoji(list: ShoppingList): String {
            val text = (list.name + " " + list.marketName.orEmpty()).lowercase(Locale("pt", "BR"))
            return when {
                text.contains("farm") || text.contains("remed") || text.contains("droga") -> "💊"
                text.contains("feira") || text.contains("hort") || text.contains("sacol") -> "🥕"
                text.contains("bicic") || text.contains("bike") || text.contains("ciclo") -> "🚲"
                text.contains("carro") || text.contains("auto") || text.contains("moto") -> "🚗"
                text.contains("viagem") || text.contains("hotel") || text.contains("camp") -> "🧳"
                text.contains("casa") || text.contains("lar") || text.contains("limpeza") -> "🏠"
                text.contains("pc") || text.contains("tecno") || text.contains("celular") || text.contains("comput") -> "💻"
                text.contains("beb") || text.contains("bar") -> "🥤"
                else -> "🛍️"
            }
        }
    }
}
