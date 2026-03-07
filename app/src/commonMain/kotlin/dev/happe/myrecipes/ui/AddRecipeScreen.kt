package dev.happe.myrecipes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.happe.myrecipes.model.Amount
import dev.happe.myrecipes.model.Ingredient
import dev.happe.myrecipes.model.MeasurementUnit
import dev.happe.myrecipes.model.Recipe
import dev.happe.myrecipes.model.RecipeIngredient
import dev.happe.myrecipes.model.Step
import dev.happe.myrecipes.model.StepType
import dev.happe.myrecipes.model.display
import dev.happe.myrecipes.util.newId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    existingRecipe: Recipe?,
    knownIngredients: List<Ingredient>,
    onSave: (Recipe) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(existingRecipe?.name ?: "") }
    var description by remember { mutableStateOf(existingRecipe?.description ?: "") }
    var servingsText by remember { mutableStateOf(existingRecipe?.servings?.toString() ?: "") }
    var tagsText by remember { mutableStateOf(existingRecipe?.tags?.joinToString(", ") ?: "") }
    var steps by remember { mutableStateOf(existingRecipe?.steps ?: emptyList()) }

    var showAddStepDialog by remember { mutableStateOf(false) }
    var addIngredientToStepId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (existingRecipe == null) "Add Recipe" else "Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Recipe name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = servingsText,
                        onValueChange = { servingsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Servings") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = { tagsText = it },
                        label = { Text("Tags (comma-separated)") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Steps", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showAddStepDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Step")
                    }
                }
            }
            itemsIndexed(steps.sortedBy { it.position }) { index, step ->
                val allKnownIngredients = (
                    knownIngredients + steps.flatMap { s -> s.ingredients.map { it.ingredient } }
                ).distinctBy { it.id }
                StepEditCard(
                    step = step,
                    index = index,
                    onUpdate = { updated -> steps = steps.map { if (it.id == updated.id) updated else it } },
                    onDelete = { steps = steps.filter { it.id != step.id } },
                    onAddIngredient = { addIngredientToStepId = step.id },
                    onDeleteIngredient = { ri ->
                        steps = steps.map { s ->
                            if (s.id == step.id) {
                                s.copy(ingredients = s.ingredients.filter { it.ingredient.id != ri.ingredient.id })
                            } else s
                        }
                    },
                    knownIngredients = allKnownIngredients,
                )
            }
            item {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val tags = tagsText.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            onSave(
                                Recipe(
                                    id = existingRecipe?.id ?: newId(),
                                    name = name.trim(),
                                    description = description.trim().ifEmpty { null },
                                    servings = servingsText.toIntOrNull(),
                                    tags = tags,
                                    steps = steps,
                                ),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank(),
                ) {
                    Text("Save Recipe")
                }
            }
        }
    }

    if (showAddStepDialog) {
        AddStepDialog(
            onDismiss = { showAddStepDialog = false },
            onAdd = { type, desc ->
                steps = steps + Step(
                    id = newId(),
                    type = type,
                    description = desc,
                    ingredients = emptyList(),
                    position = steps.size,
                )
                showAddStepDialog = false
            },
        )
    }

    addIngredientToStepId?.let { stepId ->
        val allKnownIngredients = (
            knownIngredients + steps.flatMap { s -> s.ingredients.map { it.ingredient } }
        ).distinctBy { it.id }
        AddIngredientDialog(
            knownIngredients = allKnownIngredients,
            onDismiss = { addIngredientToStepId = null },
            onAdd = { ri ->
                steps = steps.map { s ->
                    if (s.id == stepId) s.copy(ingredients = s.ingredients + ri) else s
                }
                addIngredientToStepId = null
            },
        )
    }
}

@Composable
private fun StepEditCard(
    step: Step,
    index: Int,
    knownIngredients: List<Ingredient>,
    onUpdate: (Step) -> Unit,
    onDelete: () -> Unit,
    onAddIngredient: () -> Unit,
    onDeleteIngredient: (RecipeIngredient) -> Unit,
) {
    var description by remember(step.id) { mutableStateOf(step.description) }
    var stepType by remember(step.id) { mutableStateOf(step.type) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Step ${index + 1}", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StepType.entries.forEach { t ->
                        FilterChip(
                            selected = stepType == t,
                            onClick = { stepType = t; onUpdate(step.copy(type = t)) },
                            label = { Text(t.label) },
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete step",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            OutlinedTextField(
                value = description,
                onValueChange = { description = it; onUpdate(step.copy(description = it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            step.ingredients.forEach { ri ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "• ${ri.ingredient.name}: ${ri.amount.display()}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onDeleteIngredient(ri) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove ingredient",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            TextButton(onClick = onAddIngredient) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Ingredient")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStepDialog(
    onDismiss: () -> Unit,
    onAdd: (StepType, String) -> Unit,
) {
    var type by remember { mutableStateOf(StepType.Prep) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Step") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (description.isNotBlank()) onAdd(type, description.trim()) },
                enabled = description.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIngredientDialog(
    knownIngredients: List<Ingredient>,
    onDismiss: () -> Unit,
    onAdd: (RecipeIngredient) -> Unit,
) {
    var ingredientName by remember { mutableStateOf("") }
    var isToTaste by remember { mutableStateOf(false) }
    var quantityText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(MeasurementUnit.Cup) }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    val suggestions = if (ingredientName.length >= 2) {
        knownIngredients.filter { it.name.contains(ingredientName, ignoreCase = true) }.take(5)
    } else emptyList()

    val canAdd = ingredientName.isNotBlank() && (isToTaste || quantityText.toDoubleOrNull() != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    OutlinedTextField(
                        value = ingredientName,
                        onValueChange = {
                            ingredientName = it
                            showSuggestions = it.length >= 2
                        },
                        label = { Text("Ingredient name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    DropdownMenu(
                        expanded = showSuggestions && suggestions.isNotEmpty(),
                        onDismissRequest = { showSuggestions = false },
                    ) {
                        suggestions.forEach { ingredient ->
                            DropdownMenuItem(
                                text = { Text(ingredient.name) },
                                onClick = {
                                    ingredientName = ingredient.name
                                    showSuggestions = false
                                },
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Checkbox(checked = isToTaste, onCheckedChange = { isToTaste = it })
                    Text("To taste")
                }
                if (!isToTaste) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Amount") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                        ExposedDropdownMenuBox(
                            expanded = unitMenuExpanded,
                            onExpandedChange = { unitMenuExpanded = it },
                            modifier = Modifier.weight(1f),
                        ) {
                            OutlinedTextField(
                                value = selectedUnit.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded)
                                },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            )
                            ExposedDropdownMenu(
                                expanded = unitMenuExpanded,
                                onDismissRequest = { unitMenuExpanded = false },
                            ) {
                                MeasurementUnit.entries.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.label) },
                                        onClick = { selectedUnit = unit; unitMenuExpanded = false },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (canAdd) {
                        val ingredient = knownIngredients.firstOrNull {
                            it.name.equals(ingredientName.trim(), ignoreCase = true)
                        } ?: Ingredient(id = newId(), name = ingredientName.trim())
                        val amount = if (isToTaste) {
                            Amount.ToTaste
                        } else {
                            Amount.Measured(quantityText.toDouble(), selectedUnit)
                        }
                        onAdd(RecipeIngredient(ingredient, amount))
                    }
                },
                enabled = canAdd,
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
