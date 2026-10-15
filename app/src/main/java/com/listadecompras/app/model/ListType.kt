package com.listadecompras.app.model

enum class ListType {
    SUPERMARKET, SHOPPING;

    companion object {
        fun fromStored(raw: String?): ListType = entries.firstOrNull { it.name == raw } ?: SUPERMARKET
    }
}
