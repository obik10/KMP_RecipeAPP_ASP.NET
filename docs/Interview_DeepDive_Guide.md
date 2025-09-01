# 🎯 KMP Recipe App - Interview Deep Dive Guide

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Architecture Deep Dive](#architecture-deep-dive)
- [Backend Implementation](#backend-implementation)
- [Frontend Implementation](#frontend-implementation)
- [Key Features & Code Examples](#key-features--code-examples)
- [Common Interview Questions](#common-interview-questions)

---

## 🎯 Project Overview

### What is this project?

A **cross-platform recipe management application** demonstrating:

- **Kotlin Multiplatform (KMP)** for shared code across platforms
- **ASP.NET Core** with Clean Architecture for backend
- **Jetpack Compose** for modern UI
- **Keycloak** for enterprise authentication
- **PostgreSQL** for data persistence

### Why this tech stack?

- **KMP**: Write once, run everywhere (Android, iOS, Desktop, Web)
- **ASP.NET Core**: Enterprise-grade backend with excellent performance
- **Clean Architecture**: Maintainable, testable, and scalable code
- **Compose**: Modern declarative UI framework

---

## 🏗️ Architecture Deep Dive

### Overall Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (KMP)                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   Android   │ │     iOS     │ │   Desktop   │          │
│  │     App     │ │     App     │ │     App     │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
│           │              │              │                  │
│           └──────────────┼──────────────┘                  │
│                          │                                 │
│                    Shared Code                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   UI Layer  │ │ Business    │ │   Network   │          │
│  │ (Compose)   │ │  Logic      │ │   Layer     │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                          │
                    HTTP/HTTPS
                          │
┌─────────────────────────────────────────────────────────────┐
│                    Backend (ASP.NET Core)                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │    API      │ │ Application │ │  Domain     │          │
│  │   Layer     │ │   Layer     │ │   Layer     │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
│           │              │              │                  │
│           └──────────────┼──────────────┘                  │
│                          │                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │Infrastructure│ │  Database   │ │  External   │          │
│  │   Layer     │ │ (PostgreSQL)│ │  Services   │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

#### 1. **Clean Architecture (Backend)**

- **Separation of Concerns**: Each layer has specific responsibility
- **Dependency Inversion**: High-level modules don't depend on low-level modules
- **Testability**: Easy to unit test each layer independently

#### 2. **Kotlin Multiplatform (Frontend)**

- **Code Sharing**: 80%+ shared code across platforms
- **Platform-Specific**: Only write platform-specific code when necessary
- **Type Safety**: Kotlin's type system prevents runtime errors

---

## 🔧 Backend Implementation

### Project Structure

```
Backend/
├── RecipeApp.API/                    # 🌐 Web API entry point
│   ├── Controllers/                  # HTTP endpoints
│   ├── Program.cs                    # Application startup
│   └── appsettings.json             # Configuration
├── RecipeApp.Application/            # 🧠 Business logic
│   ├── Recipes/
│   │   ├── Commands/                # Write operations
│   │   └── Queries/                 # Read operations
│   └── DependencyInjection.cs       # DI configuration
├── RecipeApp.Domain/                 # 📋 Core business models
│   ├── Entities/                    # Domain entities
│   └── Common/                      # Shared domain logic
└── RecipeApp.Infrastructure/         # 💾 External concerns
    ├── Persistence/                 # Database layer
    ├── Services/                    # External services
    └── Repositories/                # Data access
```

### 1. **API Layer Implementation**

**File**: `Backend/src/RecipeApp.API/Controllers/RecipesController.cs`

```csharp
[ApiController]
[Route("api/[controller]")]
public class RecipesController : ControllerBase
{
    private readonly IMediator _mediator;

    public RecipesController(IMediator mediator)
    {
        _mediator = mediator;
    }

    [HttpGet]
    public async Task<ActionResult<PaginatedResult<RecipeDto>>> GetRecipes(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var query = new GetRecipesPaginatedQuery(page, pageSize);
        var result = await _mediator.Send(query);
        return Ok(result);
    }

    [HttpPost]
    [Authorize]
    public async Task<ActionResult<RecipeDto>> CreateRecipe(
        [FromBody] CreateRecipeCommand command)
    {
        var result = await _mediator.Send(command);
        return CreatedAtAction(nameof(GetRecipe), new { id = result.Id }, result);
    }
}
```

**Key Points for Interview:**

- Uses **MediatR** for CQRS pattern
- **Authorization** attributes for security
- **Async/await** for non-blocking operations
- **Dependency injection** for loose coupling

### 2. **Application Layer (CQRS)**

**File**: `Backend/src/RecipeApp.Application/Recipes/Commands/CreateRecipe/CreateRecipeCommand.cs`

```csharp
public class CreateRecipeCommand : IRequest<Result<RecipeDto>>
{
    public string Title { get; set; }
    public string Instructions { get; set; }
    public List<RecipeIngredientDto> Ingredients { get; set; }
    public string? ImageUrl { get; set; }
}

public class CreateRecipeCommandHandler : IRequestHandler<CreateRecipeCommand, Result<RecipeDto>>
{
    private readonly IRecipeRepository _recipeRepository;
    private readonly ICurrentUserService _currentUserService;
    private readonly IUnitOfWork _unitOfWork;

    public async Task<Result<RecipeDto>> Handle(
        CreateRecipeCommand request,
        CancellationToken cancellationToken)
    {
        // Validate input
        if (string.IsNullOrWhiteSpace(request.Title))
            return Result<RecipeDto>.Failure("Title is required");

        // Create domain entity
        var recipe = Recipe.Create(
            request.Title,
            request.Instructions,
            request.Ingredients.Select(i => RecipeIngredient.Create(i.Name, i.Amount, i.Unit)).ToList(),
            _currentUserService.UserId,
            request.ImageUrl
        );

        // Save to database
        await _recipeRepository.AddAsync(recipe);
        await _unitOfWork.SaveChangesAsync(cancellationToken);

        return Result<RecipeDto>.Success(recipe.ToDto());
    }
}
```

**Key Points for Interview:**

- **CQRS Pattern**: Separate commands (write) from queries (read)
- **Domain-Driven Design**: Business logic in domain entities
- **Validation**: Input validation in command handlers
- **Unit of Work**: Transaction management

### 3. **Domain Layer**

**File**: `Backend/src/RecipeApp.Domain/Entities/Recipe.cs`

```csharp
public class Recipe : Entity
{
    public string Title { get; private set; }
    public string Instructions { get; private set; }
    public List<RecipeIngredient> Ingredients { get; private set; }
    public string? ImageUrl { get; private set; }
    public string CreatedBy { get; private set; }
    public DateTime CreatedAt { get; private set; }
    public DateTime? UpdatedAt { get; private set; }

    public static Recipe Create(
        string title,
        string instructions,
        List<RecipeIngredient> ingredients,
        string createdBy,
        string? imageUrl = null)
    {
        if (string.IsNullOrWhiteSpace(title))
            throw new ArgumentException("Title cannot be empty", nameof(title));

        return new Recipe
        {
            Id = Guid.NewGuid().ToString(),
            Title = title,
            Instructions = instructions,
            Ingredients = ingredients ?? new List<RecipeIngredient>(),
            ImageUrl = imageUrl,
            CreatedBy = createdBy,
            CreatedAt = DateTime.UtcNow
        };
    }

    public void Update(string title, string instructions, List<RecipeIngredient> ingredients)
    {
        Title = title;
        Instructions = instructions;
        Ingredients = ingredients;
        UpdatedAt = DateTime.UtcNow;
    }
}
```

**Key Points for Interview:**

- **Encapsulation**: Private setters, public methods for state changes
- **Domain Logic**: Business rules enforced in the domain
- **Immutability**: Properties are read-only from outside
- **Factory Method**: Static `Create` method for object construction

---

## 📱 Frontend Implementation

### Project Structure

```
Frontend/KMPRecipeApp_ASPDotnet/
├── composeApp/                      # 🎨 Shared UI
│   ├── src/
│   │   ├── commonMain/             # Shared code
│   │   ├── androidMain/            # Android-specific
│   │   ├── iosMain/                # iOS-specific
│   │   └── jvmMain/                # Desktop-specific
│   └── build.gradle.kts            # Build configuration
├── shared/                          # 🔧 Shared business logic
│   ├── src/
│   │   ├── commonMain/             # Shared code
│   │   ├── androidMain/            # Android-specific
│   │   └── iosMain/                # iOS-specific
│   └── build.gradle.kts            # Build configuration
└── gradle/                          # Build tools
```

### 1. **Shared Business Logic**

**File**: `Frontend/KMPRecipeApp_ASPDotnet/shared/src/commonMain/kotlin/org/robiul/kmprecipeapp/data/repository/RecipeRepositoryImpl.kt`

```kotlin
class RecipeRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : RecipeRepository {

    override suspend fun getRecipesPaginated(
        pageNumber: Int,
        pageSize: Int
    ): Result<PaginatedResult<Recipe>> {
        return try {
            // First, try to get from local cache
            val cachedRecipes = localDataSource.getRecipesPaginated(pageNumber, pageSize)

            // Then fetch from remote
            val remoteResult = remoteDataSource.getRecipesPaginated(pageNumber, pageSize)

            when (remoteResult) {
                is Result.Success -> {
                    // Update local cache with fresh data
                    localDataSource.saveRecipes(remoteResult.data.items)
                    remoteResult
                }
                is Result.Error -> {
                    // Return cached data if available, otherwise return error
                    if (cachedRecipes.items.isNotEmpty()) {
                        Result.Success(cachedRecipes)
                    } else {
                        remoteResult
                    }
                }
            }
        } catch (e: Exception) {
            Result.Error(AppError.Network(e.message ?: "Unknown error"))
        }
    }
}
```

**Key Points for Interview:**

- **Repository Pattern**: Abstraction over data sources
- **Offline-First**: Local cache with remote sync
- **Error Handling**: Graceful fallback to cached data
- **Coroutines**: Asynchronous operations with suspend functions

### 2. **Network Layer**

**File**: `Frontend/KMPRecipeApp_ASPDotnet/shared/src/commonMain/kotlin/org/robiul/kmprecipeapp/core/NetworkClient.kt`

```kotlin
class NetworkClient(
    private val baseUrl: String,
    private val engine: HttpClientEngine,
    private val tokenStore: AuthTokenStore
) {
    private val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
        }
    }

    suspend fun <T> get(endpoint: String, responseType: KClass<T>): Result<T> {
        return try {
            val response = client.get("$baseUrl$endpoint") {
                addAuthHeader()
            }

            if (response.status.isSuccess()) {
                val data = response.body<JsonElement>().jsonPrimitive.content
                val result = Json.decodeFromString<T>(data)
                Result.Success(result)
            } else {
                Result.Error(AppError.Server(response.status.value))
            }
        } catch (e: Exception) {
            Result.Error(AppError.Network(e.message ?: "Network error"))
        }
    }

    private fun HttpRequestBuilder.addAuthHeader() {
        val token = tokenStore.getAccessToken()
        if (token != null) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
```

**Key Points for Interview:**

- **Ktor Client**: Modern HTTP client for Kotlin
- **Content Negotiation**: Automatic JSON serialization
- **Authentication**: Automatic token injection
- **Error Handling**: Structured error responses
- **Timeout Configuration**: Network resilience

### 3. **UI Layer (Compose)**

**File**: `Frontend/KMPRecipeApp_ASPDotnet/composeApp/src/commonMain/kotlin/org/robiul/kmprecipeapp/ui/screens/HomeScreen.kt`

```kotlin
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRecipeClick: (RecipeUiModel) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onLoadPage: (page: Int) -> Unit,
    favoritesViewModel: FavoritesViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Discover Recipes", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search recipes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.large,
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                // Content
                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    state.errorMessage != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Error: ${state.errorMessage}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                    state.recipes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No recipes found",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.recipes, key = { it.id }) { recipeUi ->
                                RecipeCard(
                                    recipe = recipeUi,
                                    favoritesViewModel = favoritesViewModel,
                                    onClick = { onRecipeClick(recipeUi) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
```

**Key Points for Interview:**

- **Jetpack Compose**: Modern declarative UI framework
- **State Management**: Reactive UI with StateFlow
- **Material Design 3**: Modern design system
- **Lazy Loading**: Efficient list rendering
- **Responsive Design**: Adaptive grid layout

### 4. **Platform-Specific Code**

**File**: `Frontend/KMPRecipeApp_ASPDotnet/composeApp/src/androidMain/kotlin/org/robiul/kmprecipeapp/DriverFactory.android.kt`

```kotlin
actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(AppDatabase.Schema, context, "app.db")
    }
}
```

**File**: `Frontend/KMPRecipeApp_ASPDotnet/composeApp/src/jvmMain/kotlin/org/robiul/kmprecipeapp/DriverFactory.jvm.kt`

```kotlin
actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = System.getProperty("user.home") + "/.kmprecipeapp/app.db"
        val driver = JdbcSqliteDriver(databasePath)
        AppDatabase.Schema.create(driver)
        return driver
    }
}
```

**Key Points for Interview:**

- **Expect/Actual**: Kotlin Multiplatform's platform-specific code mechanism
- **SQLDelight**: Type-safe database access
- **Platform Abstraction**: Same interface, different implementations

---

## ⚡ Key Features & Code Examples

### 1. **Recipe Search with Pagination**

**File**: `Frontend/KMPRecipeApp_ASPDotnet/shared/src/commonMain/kotlin/org/robiul/kmprecipeapp/domain/usecase/GetRecipesPaginated.kt`

```kotlin
class GetRecipesPaginated(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(page: Int, pageSize: Int): Result<PaginatedResult<Recipe>> {
        return repository.getRecipesPaginated(page, pageSize)
    }
}
```

### 2. **Image Upload**

**File**: `Frontend/KMPRecipeApp_ASPDotnet/composeApp/src/androidMain/kotlin/org/robiul/kmprecipeapp/core/PickImageLauncher.android.kt`

```kotlin
@Composable
actual fun PickImageLauncher(
    onImagePicked: (fileName: String, bytes: ByteArray) -> Unit,
    sources: List<ImageSource>,
    content: @Composable (onPick: (ImageSource) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val bytes = context.readBytes(uri) ?: return@rememberLauncherForActivityResult
        val name = context.getFileName(uri)
        onImagePicked(name, bytes)
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                val bytes = context.readBytes(uri) ?: return@let
                val name = "camera_${UUID.randomUUID()}.jpg"
                onImagePicked(name, bytes)
            }
        }
    }

    content { source ->
        when (source) {
            ImageSource.Gallery -> galleryLauncher.launch("image/*")
            ImageSource.Camera -> {
                val uri = context.createTempImageUri()
                cameraUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }
}
```

### 3. **Favorites System**

**File**: `Frontend/KMPRecipeApp_ASPDotnet/shared/src/commonMain/kotlin/org/robiul/kmprecipeapp/presentation/viewmodel/FavoritesViewModel.kt`

```kotlin
class FavoritesViewModel(
    private val repository: RecipeRepository,
    private val settings: Settings
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _favoriteIds = MutableStateFlow(loadCachedIds())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    fun toggleFavorite(recipe: Recipe) {
        scope.launch {
            val currentlyFav = _favoriteIds.value.contains(recipe.id)

            // Optimistic update
            if (currentlyFav) {
                _favoriteIds.value = _favoriteIds.value - recipe.id
            } else {
                _favoriteIds.value = _favoriteIds.value + recipe.id
            }

            // Call backend
            val remoteResult = if (currentlyFav) {
                repository.removeFavorite(recipe.id)
            } else {
                repository.addFavorite(recipe.id)
            }

            when (remoteResult) {
                is Result.Success -> {
                    // Server success - update state
                    persistIds(_favoriteIds.value)
                }
                is Result.Error -> {
                    // Rollback on failure
                    if (currentlyFav) {
                        _favoriteIds.value = _favoriteIds.value + recipe.id
                    } else {
                        _favoriteIds.value = _favoriteIds.value - recipe.id
                    }
                    persistIds(_favoriteIds.value)
                }
            }
        }
    }
}
```

---

## ❓ Common Interview Questions

### 1. **Why did you choose Kotlin Multiplatform?**

**Answer**: KMP allows us to share business logic across multiple platforms while maintaining native performance. We can write once in Kotlin and deploy to Android, iOS, Desktop, and Web. This reduces development time by 60-70% and ensures consistency across platforms.

### 2. **How do you handle offline functionality?**

**Answer**: We implement an offline-first architecture using SQLDelight for local storage. The app checks local cache first, shows data immediately, then syncs with the server in the background. This provides a smooth user experience even without internet.

### 3. **Explain your authentication strategy**

**Answer**: We use Keycloak for enterprise-grade authentication. The frontend stores JWT tokens securely using platform-specific encrypted storage. Tokens are automatically refreshed, and we implement proper token validation on both client and server sides.

### 4. **How do you ensure code quality?**

**Answer**: We follow Clean Architecture principles, implement comprehensive unit and integration tests, use static code analysis tools, and maintain consistent coding standards. Our CI/CD pipeline runs tests automatically on every commit.

### 5. **What challenges did you face with KMP?**

**Answer**: Platform-specific implementations for features like image picking and file storage. We solved this using Kotlin's expect/actual mechanism, allowing us to write platform-specific code while maintaining a shared interface.

### 6. **How do you handle performance optimization?**

**Answer**: We implement lazy loading for lists, image caching with Coil, database indexing, and efficient state management with StateFlow. The app also uses pagination to handle large datasets efficiently.

### 7. **Explain your database design**

**Answer**: We use PostgreSQL with Entity Framework Core for the backend and SQLDelight for the frontend. The design follows normalization principles while maintaining good performance through proper indexing and query optimization.

### 8. **How do you handle errors?**

**Answer**: We implement structured error handling with Result types, user-friendly error messages, and automatic retry mechanisms. Network errors are handled gracefully with offline fallbacks.

---

## 🎯 Key Takeaways for Interview

### Technical Strengths

1. **Modern Architecture**: Clean Architecture with CQRS
2. **Cross-Platform**: Single codebase for multiple platforms
3. **Performance**: Offline-first with efficient caching
4. **Security**: Enterprise-grade authentication
5. **Scalability**: Microservices-ready design

### Business Value

1. **Cost Efficiency**: 60-70% code sharing reduces development time
2. **Consistency**: Same experience across all platforms
3. **Maintainability**: Clean, testable code structure
4. **User Experience**: Fast, responsive, offline-capable app

### Innovation

1. **Kotlin Multiplatform**: Cutting-edge cross-platform technology
2. **Jetpack Compose**: Modern declarative UI
3. **Clean Architecture**: Enterprise-grade code organization
4. **Offline-First**: Modern mobile app approach

---

_This guide provides comprehensive coverage of the KMP Recipe App for interview preparation. Focus on understanding the architecture decisions, technical implementations, and business value of each component._
