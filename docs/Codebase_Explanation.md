# 🍳 KMP Recipe App - Codebase Explanation

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Architecture Overview](#architecture-overview)
- [Backend (ASP.NET Core)](#backend-aspnet-core)
- [Frontend (Kotlin Multiplatform)](#frontend-kotlin-multiplatform)
- [Database & Data Flow](#database--data-flow)
- [Authentication System](#authentication-system)
- [Key Features Implementation](#key-features-implementation)
- [Development Setup](#development-setup)

---

## 🎯 Project Overview

This is a **cross-platform recipe application** that works on:

- 📱 **Android phones/tablets**
- 🍎 **iOS phones/tablets**
- 💻 **Desktop computers** (Windows, Mac, Linux)
- 🌐 **Web browsers** (coming soon)

**Think of it like a smart digital cookbook** that lets you:

- Browse and search recipes
- Save your favorite recipes
- Create and share your own recipes
- Upload photos of your dishes
- Manage your cooking profile

---

## 🏗️ Architecture Overview

The app is built using a **modern, scalable architecture** with two main parts:

```
┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │    Backend      │
│   (KMP App)     │◄──►│  (ASP.NET API)  │
│                 │    │                 │
│ • Android       │    │ • Recipe API    │
│ • iOS           │    │ • User Auth     │
│ • Desktop       │    │ • File Storage  │
│ • Web (soon)    │    │ • Database      │
└─────────────────┘    └─────────────────┘
```

**Why this architecture?**

- **Frontend**: One codebase works on all devices (Kotlin Multiplatform)
- **Backend**: Powerful server handles data, security, and business logic
- **Separation**: Frontend focuses on user interface, backend handles data

---

## 🔧 Backend (ASP.NET Core)

### What is ASP.NET Core?

Think of it as a **smart server** that:

- Receives requests from your phone/app
- Processes data and business logic
- Sends back responses
- Manages the database
- Handles security

### Project Structure

```
Backend/
├── RecipeApp.API/          # 🌐 Web API (entry point)
├── RecipeApp.Application/  # 🧠 Business logic
├── RecipeApp.Domain/       # 📋 Data models
└── RecipeApp.Infrastructure/ # 💾 Database & external services
```

### Key Components

#### 1. **API Layer** (`RecipeApp.API`)

```csharp
// This is where requests come in
[ApiController]
public class RecipesController : ControllerBase
{
    [HttpGet("recipes")]
    public async Task<ActionResult<List<Recipe>>> GetRecipes()
    {
        // Get recipes from database
        // Return them to the app
    }
}
```

#### 2. **Business Logic** (`RecipeApp.Application`)

```csharp
// This handles the "thinking" part
public class CreateRecipeCommandHandler
{
    public async Task<Result<Recipe>> Handle(CreateRecipeCommand command)
    {
        // Validate the recipe data
        // Save to database
        // Return success/error
    }
}
```

#### 3. **Data Models** (`RecipeApp.Domain`)

```csharp
// This defines what a recipe looks like
public class Recipe : Entity
{
    public string Title { get; set; }
    public string Instructions { get; set; }
    public List<RecipeIngredient> Ingredients { get; set; }
    public string ImageUrl { get; set; }
    public User CreatedBy { get; set; }
}
```

---

## 📱 Frontend (Kotlin Multiplatform)

### What is Kotlin Multiplatform (KMP)?

Think of it as **"write once, run everywhere"**:

- Write code in Kotlin
- It automatically works on Android, iOS, Desktop, and Web
- Share business logic across all platforms
- Only write platform-specific code when needed

### Project Structure

```
Frontend/
├── composeApp/           # 🎨 User Interface (shared)
├── shared/              # 🔧 Business Logic (shared)
├── androidApp/          # 🤖 Android-specific code
├── iosApp/              # 🍎 iOS-specific code
└── server/              # 💻 Desktop app
```

### Key Components

#### 1. **Shared Business Logic** (`shared/`)

```kotlin
// This code works on ALL platforms
class RecipeRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) {
    suspend fun getRecipes(): List<Recipe> {
        // Get from local cache first (fast)
        // Then update from server (fresh data)
        return localDataSource.getRecipes()
    }
}
```

#### 2. **User Interface** (`composeApp/`)

```kotlin
// This creates the visual screens
@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit
) {
    LazyColumn {
        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe) }
            )
        }
    }
}
```

#### 3. **Platform-Specific Code**

```kotlin
// Android-specific image picking
actual fun PickImageLauncher() {
    // Uses Android's camera/gallery
}

// iOS-specific image picking
actual fun PickImageLauncher() {
    // Uses iOS's camera/gallery
}
```

---

## 🗄️ Database & Data Flow

### How Data Flows

```
1. User opens app
   ↓
2. App checks local database (fast)
   ↓
3. App shows cached data immediately
   ↓
4. App fetches fresh data from server
   ↓
5. App updates local database
   ↓
6. App shows updated data
```

### Database Structure

```sql
-- Users table
Users (
    Id, Email, Username, CreatedAt
)

-- Recipes table
Recipes (
    Id, Title, Instructions, ImageUrl,
    CreatedBy, CreatedAt, UpdatedAt
)

-- Recipe ingredients
RecipeIngredients (
    Id, RecipeId, Name, Amount, Unit
)

-- User favorites
FavouriteRecipes (
    Id, UserId, RecipeId, CreatedAt
)
```

---

## 🔐 Authentication System

### How Login Works

```
1. User enters email/password
   ↓
2. App sends to Keycloak (security server)
   ↓
3. Keycloak validates credentials
   ↓
4. Keycloak returns access token
   ↓
5. App stores token securely
   ↓
6. App uses token for all future requests
```

### Token Management

```kotlin
// Secure token storage
class AuthTokenStore {
    fun saveTokens(accessToken: String, refreshToken: String) {
        // Store in encrypted storage
    }

    fun getAccessToken(): String? {
        // Get from secure storage
    }
}
```

---

## ⚡ Key Features Implementation

### 1. **Recipe Browsing**

```kotlin
// Get recipes with pagination
class GetRecipesPaginated {
    suspend fun execute(page: Int): PaginatedResult<Recipe> {
        return repository.getRecipesPaginated(page, PAGE_SIZE)
    }
}
```

### 2. **Recipe Creation**

```kotlin
// Create new recipe
class CreateRecipe {
    suspend fun execute(recipe: Recipe): Result<Recipe> {
        // Validate recipe data
        // Upload image if provided
        // Save to database
        // Return success/error
    }
}
```

### 3. **Image Upload**

```kotlin
// Handle image uploads
class UploadRecipeImage {
    suspend fun execute(imageBytes: ByteArray): Result<String> {
        // Convert to proper format
        // Upload to file server
        // Return image URL
    }
}
```

### 4. **Favorites System**

```kotlin
// Add/remove favorites
class AddFavorite {
    suspend fun execute(recipeId: String): Result<Unit> {
        return repository.addFavorite(recipeId)
    }
}
```

---

## 🛠️ Development Setup

### Prerequisites

- **Java 11+** (for Kotlin compilation)
- **.NET 6+** (for backend)
- **Android Studio** (for Android development)
- **Xcode** (for iOS development, Mac only)
- **Docker** (for database)

### Running the Project

#### 1. **Start Backend**

```bash
cd Backend
docker-compose up -d  # Start database
dotnet run            # Start API server
```

#### 2. **Start Frontend**

```bash
cd Frontend/KMPRecipeApp_ASPDotnet
./gradlew :composeApp:run  # Run desktop app
./gradlew androidApp:installDebug  # Install on Android
```

### Project Configuration

#### Backend Settings (`appsettings.json`)

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=localhost;Database=RecipeApp;..."
  },
  "Keycloak": {
    "AuthServerUrl": "http://localhost:8080",
    "Realm": "recipe-app"
  }
}
```

#### Frontend Settings (`Constants.kt`)

```kotlin
object Constants {
    const val BASE_URL = "http://localhost:5000"
    const val KEYCLOAK_URL = "http://localhost:8080"
}
```

---

## 🎨 UI/UX Features

### 1. **Responsive Design**

- Works on phones, tablets, and desktops
- Adapts layout based on screen size
- Touch-friendly on mobile, mouse-friendly on desktop

### 2. **Material Design 3**

- Modern, beautiful interface
- Consistent design language
- Smooth animations and transitions

### 3. **Offline Support**

- Works without internet connection
- Syncs when connection is restored
- Caches images and data locally

---

## 🔧 Technical Highlights

### 1. **Clean Architecture**

- Clear separation of concerns
- Easy to test and maintain
- Scalable and extensible

### 2. **Dependency Injection**

- Uses Koin for dependency management
- Easy to swap implementations
- Better testability

### 3. **Error Handling**

- Graceful error handling
- User-friendly error messages
- Automatic retry mechanisms

### 4. **Performance**

- Lazy loading of images
- Pagination for large lists
- Efficient database queries
- Local caching

---

## 🚀 Future Enhancements

### Planned Features

- **Web Version**: Browser-based app
- **Social Features**: Share recipes, follow users
- **Recipe Categories**: Better organization
- **Search Improvements**: Advanced filtering
- **Push Notifications**: Recipe reminders

### Technical Improvements

- **Real-time Updates**: Live recipe changes
- **Advanced Caching**: Better offline experience
- **Performance Optimization**: Faster loading
- **Security Enhancements**: Better token management

---

## 📚 Learning Resources

### For Developers

- **Kotlin Multiplatform**: [Official Docs](https://kotlinlang.org/docs/multiplatform.html)
- **Jetpack Compose**: [Android Docs](https://developer.android.com/jetpack/compose)
- **ASP.NET Core**: [Microsoft Docs](https://docs.microsoft.com/en-us/aspnet/core/)
- **Clean Architecture**: [Uncle Bob's Blog](https://blog.cleancoder.com/)

### For Users

- **User Guide**: How to use the app features
- **FAQ**: Common questions and answers
- **Support**: Contact information for help

---

## 🤝 Contributing

### How to Contribute

1. **Fork** the repository
2. **Create** a feature branch
3. **Make** your changes
4. **Test** thoroughly
5. **Submit** a pull request

### Code Standards

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Write unit tests for new features

---

_This document provides a comprehensive overview of the KMP Recipe App codebase. For detailed technical information, refer to the inline code comments and API documentation._
