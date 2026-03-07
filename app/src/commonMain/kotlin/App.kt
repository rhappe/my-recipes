package dev.happe.myrecipes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.happe.myrecipes.model.Ingredient
import dev.happe.myrecipes.model.Recipe
import dev.happe.myrecipes.network.ApiService
import dev.happe.myrecipes.network.dto.IngredientDto
import dev.happe.myrecipes.repository.AppRepository
import dev.happe.myrecipes.ui.AddRecipeScreen
import dev.happe.myrecipes.ui.LoginScreen
import dev.happe.myrecipes.ui.RecipeDetailScreen
import dev.happe.myrecipes.ui.RecipeListScreen
import kotlinx.coroutines.launch

private sealed class Screen {
    data object Login : Screen()
    data object RecipeList : Screen()
    data class RecipeDetail(val recipeId: String) : Screen()
    data object AddRecipe : Screen()
    data class EditRecipe(val recipeId: String) : Screen()
}

@Composable
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf<Screen>(Screen.Login) }

        var repository by remember { mutableStateOf<AppRepository?>(null) }
        var loginLoading by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf<String?>(null) }

        var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
        var serverIngredients by remember { mutableStateOf<List<IngredientDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        val repo = repository

        // Reload from server each time the list screen is shown
        LaunchedEffect(screen, repo) {
            if (screen == Screen.RecipeList && repo != null) {
                isLoading = true
                try {
                    recipes = repo.loadRecipes()
                    serverIngredients = repo.loadIngredients()
                } catch (_: Exception) {
                } finally {
                    isLoading = false
                }
            }
        }

        val knownIngredients: List<Ingredient> = remember(serverIngredients, recipes) {
            val fromServer = serverIngredients.map { Ingredient(id = it.id, name = it.name) }
            val fromLocal = recipes.flatMap { r -> r.steps.flatMap { s -> s.ingredients.map { it.ingredient } } }
            (fromServer + fromLocal).distinctBy { it.id }
        }

        when (val s = screen) {
            is Screen.Login -> LoginScreen(
                isLoading = loginLoading,
                error = loginError,
                onLogin = { serverUrl, username, password ->
                    scope.launch {
                        loginLoading = true
                        loginError = null
                        try {
                            val token = ApiService(serverUrl).login(username, password).token
                            repository = AppRepository(ApiService(serverUrl, token))
                            screen = Screen.RecipeList
                        } catch (e: Exception) {
                            loginError = e.message ?: "Login failed"
                        } finally {
                            loginLoading = false
                        }
                    }
                },
            )

            is Screen.RecipeList -> {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    RecipeListScreen(
                        recipes = recipes,
                        onAddRecipe = { screen = Screen.AddRecipe },
                        onOpenRecipe = { screen = Screen.RecipeDetail(it.id) },
                    )
                }
            }

            is Screen.RecipeDetail -> {
                val recipe = recipes.firstOrNull { it.id == s.recipeId }
                if (recipe != null) {
                    RecipeDetailScreen(
                        recipe = recipe,
                        onBack = { screen = Screen.RecipeList },
                        onEdit = { screen = Screen.EditRecipe(recipe.id) },
                        onDelete = {
                            scope.launch {
                                repo?.deleteRecipe(recipe.id)
                                recipes = recipes.filter { it.id != recipe.id }
                                screen = Screen.RecipeList
                            }
                        },
                    )
                }
            }

            is Screen.AddRecipe -> AddRecipeScreen(
                existingRecipe = null,
                knownIngredients = knownIngredients,
                onSave = { recipe ->
                    scope.launch {
                        isLoading = true
                        try {
                            val saved = repo!!.saveRecipe(recipe, serverIngredients)
                            serverIngredients = repo.loadIngredients()
                            recipes = recipes + saved
                            screen = Screen.RecipeDetail(saved.id)
                        } catch (_: Exception) {
                        } finally {
                            isLoading = false
                        }
                    }
                },
                onCancel = { screen = Screen.RecipeList },
            )

            is Screen.EditRecipe -> {
                val recipe = recipes.firstOrNull { it.id == s.recipeId }
                if (recipe != null) {
                    AddRecipeScreen(
                        existingRecipe = recipe,
                        knownIngredients = knownIngredients,
                        onSave = { updated ->
                            scope.launch {
                                isLoading = true
                                try {
                                    val saved = repo!!.updateRecipe(updated, serverIngredients)
                                    serverIngredients = repo.loadIngredients()
                                    recipes = recipes.map { if (it.id == saved.id) saved else it }
                                    screen = Screen.RecipeDetail(saved.id)
                                } catch (_: Exception) {
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        onCancel = { screen = Screen.RecipeDetail(recipe.id) },
                    )
                }
            }
        }
    }
}
