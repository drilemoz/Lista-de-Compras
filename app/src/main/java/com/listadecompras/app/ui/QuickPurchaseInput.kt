package com.listadecompras.app.ui

/** Validates a complete user-entered quantity or BRL amount. Never treats an empty draft as zero. */
internal object QuickPurchaseInput {
    fun parseNonNegative(text: String?): Double? {
        val raw = text?.trim().orEmpty()
        if (raw.isEmpty()) return null
        val normalized = if (',' in raw) {
            // Dot is allowed only as a correctly grouped thousands separator.
            if (!raw.matches(Regex("[0-9]+,[0-9]+")) &&
                !raw.matches(Regex("[0-9]{1,3}(\\.[0-9]{3})+,[0-9]+"))) return null
            raw.replace(".", "").replace(',', '.')
        } else raw
        if (!normalized.matches(Regex("[0-9]+(\\.[0-9]+)?"))) return null
        return normalized.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
    }
}
