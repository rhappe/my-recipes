package dev.happe.myrecipes.model

enum class IngredientCategory(val label: String) {
    Produce("Produce"),
    Dairy("Dairy"),
    Meat("Meat"),
    Seafood("Seafood"),
    Bakery("Bakery"),
    Spices("Spices"),
    Pantry("Pantry"),
    Beverages("Beverages"),
    Frozen("Frozen"),
    Other("Other"),
}

data class Ingredient(
    val id: String,
    val name: String,
    val category: IngredientCategory? = null,
    val defaultUnit: MeasurementUnit? = null,
)
