Project Plan — Clean Code & Clean Architecture Recipe App

Objective
Build a cross-platform recipe app (Android-first, with optional iOS/Desktop/Web) that:
• Displays recipes from TheMealDB API (public API)
• Displays user-created recipes stored in your backend
• Merges both into a single homepage feed
• Allows users to create, edit, delete only their own recipes
• Supports image uploads for user recipes
• Uses Keycloak + JWT authentication for secure actions
• Caches recipes for offline us

Phase 1 — Foundation & Public Feed
Goal: Get the app skeleton running and display recipes from TheMealDB.
• Frontend (KMP)
o Create Kotlin Multiplatform project with shared module + Android app module.
o Set up:
 Koin for dependency injection
 Ktor client for networking
 SQLDelight for local caching
o Create TheMealDbRepository to fetch recipes from TheMealDB API.
o Implement:
 Homepage list screen (read-only from TheMealDB)
 Recipe detail screen (public API)
• Backend
o Set up ASP.NET Core Web API + MySQL + Entity Framework Core.
o Create Recipe entity, migration, repository, and controller.
o Implement GET /recipes (returns empty list for now, no auth).
o Add Swagger for quick testing.

---

Phase 2 — User Recipes CRUD (No Auth Yet)
Goal: Let the user create, edit, delete their own recipes, but without auth for now.
• Backend
o Implement:
 GET /recipes (returns all user recipes from DB)
 POST /recipes (create recipe, store image on local file system)
 PUT /recipes/{id} (update recipe)
 DELETE /recipes/{id} (delete recipe)
o For now, no JWT checks — assume all calls are allowed.
• Frontend (KMP)
o Create UserRecipeRepository to call backend APIs.
o Merge TheMealDB recipes + user recipes in the homepage feed.
o Add “My Recipes” tab → filters only user-created recipes.
o Add Add/Edit/Delete screens for user recipes.
o Implement image picker (camera/gallery) for Android only.

---

Phase 3 — Authentication
Goal: Secure all write operations with Keycloak + JWT.
• Backend
o Configure Keycloak realm, client, and users.
o Implement JWT authentication middleware in ASP.NET Core.
o Protect:
 POST /recipes
 PUT /recipes/{id}
 DELETE /recipes/{id}
o In update/delete endpoints, validate that CreatedByUserId == JWT user ID.
• Frontend (KMP)
o Add login screen (username/password → Keycloak /token).
o Store tokens in platform-secure storage.
o Add Ktor interceptor to attach Authorization: Bearer <token> to backend calls.
o Show Edit/Delete buttons only for recipes owned by the logged-in user.

---

Phase 4 — Offline Caching
Goal: Allow browsing recipes offline and syncing user recipes when back online.
• Frontend (KMP)
o Save both TheMealDB and user recipes in SQLDelight.
o When offline:
 Show cached feed
 Mark new/edited recipes as syncPending
o When online:
 Sync pending changes to backend.
• Backend
o No major changes — just handle incoming updates from synced clients.

---

Phase 5 — Polish & Multi-Platform Support
Goal: Make the app stable, clean, and prepare it for iOS/Desktop/Web if possible.
• Frontend
o Add loading/error states to all screens.
o Improve UI (Jetpack Compose on Android, SwiftUI on iOS if time permits).
o Try minimal builds for iOS/Desktop/Web using shared logic.
• Backend
o Finalize Swagger API docs.
o Add README + setup instructions for local backend run.
• Testing
o Test CRUD + auth + offline flow end-to-end.
o Fix last-minute bugs.

2. Tech Stack
   Backend:
   - Framework: ASP .Net Core Web API
   - Database: MySQL
   - ORM: Entity Framework Core
   - Auth: Keycloak (JWT-based authentication)
   - Storage: Local file system (for recipe photos)
     Frontend:
   - Language: Kotlin Multiplatform
   - Architecture: Clean Architecture (Domain, Data, Presentation layers)
   - Dependency Injection: Koin
   - Networking: Ktor
   - Local Database: SQLDelight
     UI:
   - Android: Jetpack Compose
   - iOS: SwiftUI
   - Desktop: Compose Multiplatform
   - Web: Compose for Web
3. Deliverables
   Backend:
   - API endpoints for recipe CRUD with authentication
   - Image upload endpoint
   - API documentation (Swagger)
     Frontend:
   - Cross-platform recipe app with CRUD & photo support
   - Offline caching via SQLDelight
   - Clean code with layered architecture
     Testing:
   - Backend: Unit + integration tests
   - Frontend: Unit tests for shared logic
     Documentation:
   - README with setup instructions
   - API usage guide
