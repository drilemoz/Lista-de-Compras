package com.listadecompras.app.data

import com.listadecompras.app.model.ShoppingItem
import com.listadecompras.app.util.InstallmentCalculator
import org.json.JSONObject

/** Shared versioned optional fields; old JSON remains valid with supermarket defaults. */
internal fun JSONObject.putShoppingFields(item: ShoppingItem): JSONObject = apply {
    put("imageFile", item.imageFile)
    put("productUrl", item.productUrl)
    put("storeName", item.storeName)
    put("variant", item.variant)
    put("installmentCount", item.installmentCount)
    put("installmentAmount", item.installmentAmount)
    put("downPayment", item.downPayment)
    put("shipping", item.shipping)
    put("extraFees", item.extraFees)
    put("paidInstallments", item.paidInstallments)
    put("firstDueDate", item.firstDueDate)
    put("alternativeStore", item.alternativeStore)
    put("alternativeUrl", item.alternativeUrl)
    put("alternativeCashPrice", item.alternativeCashPrice ?: JSONObject.NULL)
    put("alternativeInstallments", item.alternativeInstallments)
    put("alternativeInstallmentAmount", item.alternativeInstallmentAmount)
    put("alternativeDownPayment", item.alternativeDownPayment)
    put("alternativeShipping", item.alternativeShipping)
    put("alternativeFees", item.alternativeFees)
}

internal fun ShoppingItem.readShoppingFields(json: JSONObject, strict: Boolean = false): ShoppingItem = apply {
    imageFile = json.optString("imageFile", "")
    productUrl = json.optString("productUrl", "")
    storeName = json.optString("storeName", "")
    variant = json.optString("variant", "")
    installmentCount = json.optInt("installmentCount", 0)
    installmentAmount = json.optDouble("installmentAmount", 0.0)
    downPayment = json.optDouble("downPayment", 0.0)
    shipping = json.optDouble("shipping", 0.0)
    extraFees = json.optDouble("extraFees", 0.0)
    paidInstallments = json.optInt("paidInstallments", 0)
    firstDueDate = json.optString("firstDueDate", "")
    alternativeStore = json.optString("alternativeStore", "")
    alternativeUrl = json.optString("alternativeUrl", "")
    alternativeCashPrice = if (json.isNull("alternativeCashPrice")) null else json.optDouble("alternativeCashPrice", Double.NaN)
    alternativeInstallments = json.optInt("alternativeInstallments", 0)
    alternativeInstallmentAmount = json.optDouble("alternativeInstallmentAmount", 0.0)
    alternativeDownPayment = json.optDouble("alternativeDownPayment", 0.0)
    alternativeShipping = json.optDouble("alternativeShipping", 0.0)
    alternativeFees = json.optDouble("alternativeFees", 0.0)
    if (strict) {
        require(ProductImages.validName(imageFile)) { "Nome da imagem inválido no backup." }
        require(InstallmentCalculator.validUrl(productUrl) && InstallmentCalculator.validUrl(alternativeUrl)) {
            "O backup contém um link de produto inválido."
        }
        require(storeName.length <= 120 && variant.length <= 120 && alternativeStore.length <= 120) {
            "O backup contém textos de produto acima do limite."
        }
        require(InstallmentCalculator.calculate(unitPrice, installmentCount, installmentAmount,
            downPayment, shipping, extraFees, paidInstallments) != null) {
            "O backup contém parcelas ou valores inválidos."
        }
        require(InstallmentCalculator.validDueDate(firstDueDate)) { "O backup contém data de vencimento inválida." }
        require(alternativeInstallments in 0..120 && InstallmentCalculator.validAmount(alternativeInstallmentAmount) &&
            listOf(alternativeDownPayment, alternativeShipping, alternativeFees).all(InstallmentCalculator::validAmount) &&
            (alternativeInstallments > 0 || alternativeInstallmentAmount == 0.0) &&
            (alternativeInstallments == 0 || alternativeInstallmentAmount > 0.0) &&
            (alternativeCashPrice == null || InstallmentCalculator.validAmount(alternativeCashPrice!!))) {
            "O backup contém uma comparação de oferta inválida."
        }
    }
}
