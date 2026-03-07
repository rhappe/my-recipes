package dev.happe.myrecipes.model

sealed class Amount {
    data class Measured(val quantity: Double, val unit: MeasurementUnit) : Amount()
    data object ToTaste : Amount()
}

fun Amount.display(): String = when (this) {
    is Amount.ToTaste -> "to taste"
    is Amount.Measured -> "${formatQuantity(quantity)} ${unit.label}"
}

private fun formatQuantity(q: Double): String {
    val asLong = q.toLong()
    return if (q == asLong.toDouble()) asLong.toString() else q.toString()
}
