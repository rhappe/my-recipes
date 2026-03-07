# My Recipes

A personal family recipes app with pantry inventory and shopping list, built with Kotlin Compose Multiplatform (Android, iOS, Web).

## Architecture

| Piece | Repo | Stack |
|---|---|---|
| App | this repo | Kotlin Compose Multiplatform (Android · iOS · wasmJs) |
| Server | `my-recipes-server` | Ktor · PostgreSQL · Exposed · JWT |

The app talks to a self-hosted Ktor backend. Models are duplicated between app and server; the server's OpenAPI spec is the formal contract.

## Features

### Phase 1 — Recipes ✅
- `Ingredient` entity with category and default unit
- `Recipe` with ordered steps (Prep / Cook)
- Step builder: attach ingredients inline per step
- Recipe-level ingredient list auto-derived and deduplicated from steps
- "To taste" amounts supported throughout
- Recipe list, detail, add, and edit views
- Login screen with configurable server URL

### Phase 2 — Pantry _(planned)_
- `PantryItem` CRUD
- Recipe readiness badge (have / missing breakdown)
- "Mark as made" deducts ingredients from pantry

### Phase 3 — Shopping List _(planned)_
- Generate shopping list from missing ingredients
- Check-off flow
- Completion merges purchased amounts into pantry

## Data Model

```
Ingredient      — id, name, category, defaultUnit
Amount          — Measured(quantity, unit) | ToTaste
Recipe          — id, name, description, servings, tags, steps
Step            — id, type (Prep|Cook), description, ingredients, position
RecipeIngredient — ingredient, amount
```

The recipe-level ingredient list is **derived** from step ingredients — users never edit it directly.

## Running the Web App (dev)

```bash
source ~/.sdkman/bin/sdkman-init.sh
./gradlew :app:wasmJsBrowserDevelopmentRun
# → http://localhost:8080
```

The app will prompt for a server URL, username, and password on first load. Point it at a running instance of `my-recipes-server`.
