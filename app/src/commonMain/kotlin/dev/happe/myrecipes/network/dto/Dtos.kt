package dev.happe.myrecipes.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class IngredientDto(
    val id: String,
    val name: String,
    val category: String? = null,
    val defaultUnit: String? = null,
)

@Serializable
data class CreateIngredientRequest(
    val name: String,
    val category: String? = null,
    val defaultUnit: String? = null,
)

@Serializable
data class AmountDto(
    val type: String,           // "measured" | "to_taste"
    val quantity: Double? = null,
    val unit: String? = null,
)

@Serializable
data class RecipeIngredientDto(
    val ingredient: IngredientDto,
    val amount: AmountDto,
)

@Serializable
data class StepDto(
    val id: String,
    val type: String,
    val description: String,
    val ingredients: List<RecipeIngredientDto>,
    val position: Int,
)

@Serializable
data class RecipeDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val servings: Int? = null,
    val tags: List<String>,
    val steps: List<StepDto>,
)

@Serializable
data class CreateRecipeIngredientRequest(
    val ingredientId: String,
    val amount: AmountDto,
)

@Serializable
data class CreateStepRequest(
    val type: String,
    val description: String,
    val ingredients: List<CreateRecipeIngredientRequest> = emptyList(),
    val position: Int,
)

@Serializable
data class CreateRecipeRequest(
    val name: String,
    val description: String? = null,
    val servings: Int? = null,
    val tags: List<String> = emptyList(),
    val steps: List<CreateStepRequest> = emptyList(),
)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class TokenResponse(val token: String)
