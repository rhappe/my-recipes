package dev.happe.myrecipes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.happe.myrecipes.model.Recipe
import dev.happe.myrecipes.model.RecipeIngredient
import dev.happe.myrecipes.model.Step
import dev.happe.myrecipes.model.display

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (recipe.description != null) {
                        Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (recipe.servings != null) {
                        Text(
                            "Serves ${recipe.servings}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (recipe.tags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            recipe.tags.forEach { tag ->
                                AssistChip(onClick = {}, label = { Text(tag) })
                            }
                        }
                    }
                }
            }

            // Aggregated ingredients
            if (recipe.ingredients.isNotEmpty()) {
                item {
                    Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                }
                items(recipe.ingredients) { ri -> IngredientRow(ri) }
            }

            // Steps
            if (recipe.steps.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Steps", style = MaterialTheme.typography.titleMedium)
                }
                items(recipe.steps.sortedBy { it.position }) { step ->
                    StepCard(step)
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ri: RecipeIngredient) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(ri.ingredient.name, style = MaterialTheme.typography.bodyMedium)
        Text(
            ri.amount.display(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepCard(step: Step) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Badge { Text(step.type.label) }
                Text(
                    "Step ${step.position + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(step.description, style = MaterialTheme.typography.bodyMedium)
            if (step.ingredients.isNotEmpty()) {
                step.ingredients.forEach { ri ->
                    Text(
                        "• ${ri.ingredient.name}: ${ri.amount.display()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
