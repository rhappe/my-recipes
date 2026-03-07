package dev.happe.myrecipes.network

import dev.happe.myrecipes.network.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiService(private val baseUrl: String, token: String? = null) {
    private val client: HttpClient = buildHttpClient(token)

    suspend fun login(username: String, password: String): TokenResponse =
        client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.body()

    suspend fun getRecipes(): List<RecipeDto> =
        client.get("$baseUrl/recipes").body()

    suspend fun createRecipe(request: CreateRecipeRequest): RecipeDto =
        client.post("$baseUrl/recipes") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateRecipe(id: String, request: CreateRecipeRequest): RecipeDto =
        client.put("$baseUrl/recipes/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteRecipe(id: String) {
        client.delete("$baseUrl/recipes/$id")
    }

    suspend fun getIngredients(): List<IngredientDto> =
        client.get("$baseUrl/ingredients").body()

    suspend fun createIngredient(request: CreateIngredientRequest): IngredientDto =
        client.post("$baseUrl/ingredients") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
