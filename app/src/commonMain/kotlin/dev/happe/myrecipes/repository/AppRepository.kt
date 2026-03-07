package dev.happe.myrecipes.repository

import dev.happe.myrecipes.model.Amount
import dev.happe.myrecipes.model.Ingredient
import dev.happe.myrecipes.model.IngredientCategory
import dev.happe.myrecipes.model.MeasurementUnit
import dev.happe.myrecipes.model.Recipe
import dev.happe.myrecipes.model.RecipeIngredient
import dev.happe.myrecipes.model.Step
import dev.happe.myrecipes.model.StepType
import dev.happe.myrecipes.network.ApiService
import dev.happe.myrecipes.network.dto.AmountDto
import dev.happe.myrecipes.network.dto.CreateIngredientRequest
import dev.happe.myrecipes.network.dto.CreateRecipeIngredientRequest
import dev.happe.myrecipes.network.dto.CreateRecipeRequest
import dev.happe.myrecipes.network.dto.CreateStepRequest
import dev.happe.myrecipes.network.dto.IngredientDto
import dev.happe.myrecipes.network.dto.RecipeDto

class AppRepository(private val api: ApiService) {

    suspend fun loadRecipes(): List<Recipe> =
        api.getRecipes().map { it.toDomain() }

    suspend fun loadIngredients(): List<IngredientDto> =
        api.getIngredients()

    /**
     * Saves a recipe, creating any new ingredients on the server first.
     * [serverIngredients] is the current known list from the server — used to
     * match locally-typed ingredient names to existing server IDs.
     */
    suspend fun saveRecipe(recipe: Recipe, serverIngredients: List<IngredientDto>): Recipe {
        val resolvedIds = resolveIngredients(recipe, serverIngredients)
        return api.createRecipe(recipe.toCreateRequest(resolvedIds)).toDomain()
    }

    suspend fun updateRecipe(recipe: Recipe, serverIngredients: List<IngredientDto>): Recipe {
        val resolvedIds = resolveIngredients(recipe, serverIngredients)
        return api.updateRecipe(recipe.id, recipe.toCreateRequest(resolvedIds)).toDomain()
    }

    suspend fun deleteRecipe(id: String) = api.deleteRecipe(id)

    // ── Ingredient resolution ────────────────────────────────────────────────

    /**
     * For every ingredient referenced in [recipe]'s steps, find or create a
     * server-side ingredient. Returns a map of localId → serverId.
     */
    private suspend fun resolveIngredients(
        recipe: Recipe,
        serverIngredients: List<IngredientDto>,
    ): Map<String, String> {
        val known = serverIngredients.toMutableList()
        val resolved = mutableMapOf<String, String>()

        recipe.steps.flatMap { it.ingredients }.distinctBy { it.ingredient.id }.forEach { ri ->
            val localId = ri.ingredient.id
            val existing = known.firstOrNull { it.name.equals(ri.ingredient.name, ignoreCase = true) }
            if (existing != null) {
                resolved[localId] = existing.id
            } else {
                val created = api.createIngredient(
                    CreateIngredientRequest(
                        name = ri.ingredient.name,
                        category = ri.ingredient.category?.name,
                        defaultUnit = ri.ingredient.defaultUnit?.label,
                    ),
                )
                resolved[localId] = created.id
                known.add(created)
            }
        }
        return resolved
    }

    // ── Domain → DTO ─────────────────────────────────────────────────────────

    private fun Recipe.toCreateRequest(resolvedIds: Map<String, String>) = CreateRecipeRequest(
        name = name,
        description = description,
        servings = servings,
        tags = tags,
        steps = steps.map { step ->
            CreateStepRequest(
                type = step.type.name,
                description = step.description,
                position = step.position,
                ingredients = step.ingredients.map { ri ->
                    CreateRecipeIngredientRequest(
                        ingredientId = resolvedIds[ri.ingredient.id] ?: ri.ingredient.id,
                        amount = ri.amount.toDto(),
                    )
                },
            )
        },
    )

    private fun Amount.toDto(): AmountDto = when (this) {
        is Amount.Measured -> AmountDto(type = "measured", quantity = quantity, unit = unit.label)
        Amount.ToTaste -> AmountDto(type = "to_taste")
    }

    // ── DTO → Domain ─────────────────────────────────────────────────────────

    private fun RecipeDto.toDomain() = Recipe(
        id = id,
        name = name,
        description = description,
        servings = servings,
        tags = tags,
        steps = steps.map { it.toDomain() },
    )

    private fun dev.happe.myrecipes.network.dto.StepDto.toDomain() = Step(
        id = id,
        type = StepType.valueOf(type),
        description = description,
        ingredients = ingredients.map { it.toDomain() },
        position = position,
    )

    private fun dev.happe.myrecipes.network.dto.RecipeIngredientDto.toDomain() = RecipeIngredient(
        ingredient = ingredient.toDomain(),
        amount = amount.toDomain(),
    )

    private fun IngredientDto.toDomain() = Ingredient(
        id = id,
        name = name,
        category = category?.let { c -> IngredientCategory.entries.firstOrNull { it.name == c } },
        defaultUnit = defaultUnit?.let { u -> MeasurementUnit.entries.firstOrNull { it.label == u } },
    )

    private fun AmountDto.toDomain(): Amount = when (type) {
        "measured" -> Amount.Measured(
            quantity = quantity ?: 0.0,
            unit = MeasurementUnit.entries.firstOrNull { it.label == unit } ?: MeasurementUnit.Whole,
        )
        else -> Amount.ToTaste
    }
}
