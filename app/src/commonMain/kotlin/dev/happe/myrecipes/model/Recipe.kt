package dev.happe.myrecipes.model

data class RecipeIngredient(
    val ingredient: Ingredient,
    val amount: Amount,
)

enum class StepType(val label: String) {
    Prep("Prep"),
    Cook("Cook"),
}

data class Step(
    val id: String,
    val type: StepType,
    val description: String,
    val ingredients: List<RecipeIngredient>,
    val position: Int,
)

data class Recipe(
    val id: String,
    val name: String,
    val description: String? = null,
    val servings: Int? = null,
    val tags: List<String> = emptyList(),
    val steps: List<Step> = emptyList(),
) {
    /** Aggregated ingredient list derived from all steps, deduplicating by ingredient id. */
    val ingredients: List<RecipeIngredient>
        get() = steps.flatMap { it.ingredients }
            .groupBy { it.ingredient.id }
            .map { (_, items) ->
                RecipeIngredient(
                    ingredient = items.first().ingredient,
                    amount = sumAmounts(items.map { it.amount }),
                )
            }
}

private fun sumAmounts(amounts: List<Amount>): Amount {
    val measured = amounts.filterIsInstance<Amount.Measured>()
    if (measured.isEmpty()) return Amount.ToTaste
    // Sum items that share the same unit; if mixed units, return the first
    val byUnit = measured.groupBy { it.unit }
    return if (byUnit.size == 1) {
        val unit = byUnit.keys.first()
        Amount.Measured(byUnit.getValue(unit).sumOf { it.quantity }, unit)
    } else {
        measured.first()
    }
}
