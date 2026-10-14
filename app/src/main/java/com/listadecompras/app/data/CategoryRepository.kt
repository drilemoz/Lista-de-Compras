package com.listadecompras.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Catálogo local de categorias. Mantém ícone e nome mesmo sem produtos associados. */
data class PurchaseCategory(val name: String, val icon: String, val custom: Boolean = false) {
    override fun toString(): String = name
}

class CategoryRepository(context: Context) : SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE categories (name TEXT PRIMARY KEY COLLATE NOCASE, icon TEXT NOT NULL, custom INTEGER NOT NULL DEFAULT 0)")
        defaults.forEach { category ->
            db.insertOrThrow("categories", null, category.values())
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Reservado para migrações incrementais. Nunca apagar categorias cadastradas.
    }

    fun all(): List<PurchaseCategory> {
        val categories = mutableListOf<PurchaseCategory>()
        readableDatabase.query("categories", arrayOf("name", "icon", "custom"), null, null, null, null,
            "custom ASC, name COLLATE NOCASE ASC").use { cursor ->
            while (cursor.moveToNext()) categories.add(
                PurchaseCategory(cursor.getString(0), cursor.getString(1), cursor.getInt(2) != 0)
            )
        }
        return categories
    }

    /** Retorna a categoria existente quando há correspondência sem diferenciar maiúsculas. */
    fun create(name: String, icon: String): PurchaseCategory {
        val cleaned = name.trim().replace(Regex("\\s+"), " ")
        require(cleaned.length in 2..40) { "Use entre 2 e 40 caracteres para a categoria." }
        require(!cleaned.contains(Regex("[\\p{Cntrl}<>]"))) { "Nome contém caracteres inválidos." }
        require(icon in icons) { "Selecione um dos ícones disponíveis." }
        val existing = all().firstOrNull { it.name.equals(cleaned, ignoreCase = true) }
        if (existing != null) return existing
        val value = PurchaseCategory(cleaned, icon, true)
        writableDatabase.insertOrThrow("categories", null, value.values())
        invalidateIconCache()
        return value
    }

    /** Restauro ou importação: não sobrescreve categorias existentes com o mesmo nome. */
    fun merge(values: List<PurchaseCategory>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            values.forEach { value ->
                val cleaned = value.name.trim().replace(Regex("\\s+"), " ")
                if (cleaned.length in 2..40 && !cleaned.contains(Regex("[\\p{Cntrl}<>]"))) {
                    val safeIcon = value.icon.takeIf { it in icons } ?: "🛒"
                    db.insertWithOnConflict("categories", null,
                        PurchaseCategory(cleaned, safeIcon, value.custom).values(), SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            invalidateIconCache()
        }
    }

    /** Volta às categorias padrão antes de restaurar um backup em modo substituir. */
    fun resetAndMerge(values: List<PurchaseCategory>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("categories", "custom = 1", null)
            values.forEach { value ->
                val cleaned = value.name.trim().replace(Regex("\\s+"), " ")
                if (cleaned.length in 2..40 && !cleaned.contains(Regex("[\\p{Cntrl}<>]"))) {
                    db.insertWithOnConflict("categories", null,
                        PurchaseCategory(cleaned, value.icon.takeIf { it in icons } ?: "🛒", value.custom).values(),
                        SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction(); invalidateIconCache() }
    }

    companion object {
        @Volatile private var iconCache: Map<String, String>? = null

        fun label(context: Context, category: String): String {
            val name = category.ifBlank { "Outros" }
            val iconsByName = iconCache ?: synchronized(this) {
                iconCache ?: CategoryRepository(context).use { repository ->
                    repository.all().associate { it.name.lowercase(java.util.Locale.ROOT) to it.icon }
                        .also { iconCache = it }
                }
            }
            val icon = iconsByName[name.lowercase(java.util.Locale.ROOT)] ?: "🛒"
            return "$icon  $name"
        }

        private fun invalidateIconCache() { iconCache = null }

        private const val DB_NAME = "lista_de_compras_categories.db"
        private const val DB_VERSION = 1
        val icons = listOf("🛒", "🥕", "🥩", "🥖", "🧀", "🫘", "🥤", "🧊", "🧹", "🧴", "🐾", "💊", "👶", "🏠", "🍎", "☕", "🍰", "🌿", "⚡", "🎁")
        val defaults = listOf(
            PurchaseCategory("Hortifruti", "🥕"), PurchaseCategory("Açougue", "🥩"),
            PurchaseCategory("Padaria", "🥖"), PurchaseCategory("Laticínios", "🧀"),
            PurchaseCategory("Mercearia", "🫘"), PurchaseCategory("Bebidas", "🥤"),
            PurchaseCategory("Congelados", "🧊"), PurchaseCategory("Limpeza", "🧹"),
            PurchaseCategory("Higiene", "🧴"), PurchaseCategory("Pet", "🐾"),
            PurchaseCategory("Outros", "🛒")
        )
    }
}

private fun PurchaseCategory.values(): ContentValues = ContentValues().apply {
    put("name", name)
    put("icon", icon)
    put("custom", if (custom) 1 else 0)
}
