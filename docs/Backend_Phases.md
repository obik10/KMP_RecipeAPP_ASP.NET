📌 Project: KMP + ASP.NET Core Recipe App
Architecture: Clean Architecture + Clean Code principles
Backend: ASP.NET Core Web API (.NET 8, EF Core MySQL)
Frontend: Kotlin Multiplatform (KMP) shared module + Android/iOS/web/desktop
Current Phase: Completed Phase 1 and Phase 2
________________________________________
Phase 1 — Initial Setup
Goal: Prepare the repository, environment, and base documentation for backend and frontend work.
✅ Deliverables
•	Repository Structure:
•	backend/
•	frontend/
•	docs/
•	Database:
o	MySQL in Docker container.
o	DBeaver set up for DB browsing.
o	Example .env or connection string for development.
•	Git configuration:
o	.gitignore for .NET, Kotlin, and IDE files.
o	Commit conventions agreed upon.
•	Docs:
o	Initial architecture overview.
o	Tech stack list and reasoning.
o	Phase-by-phase plan.
________________________________________
Phase 2 — Backend Foundation
Goal: Scaffold backend following Clean Architecture & Clean Code principles, connect to MySQL, and provide a testable API.
✅ Deliverables
1. Solution & Project Structure
•	Solution: RecipeApp.sln
•	Projects:
o	Domain — Entities, Value Objects (pure, no dependencies)
o	Application — Use cases, DTOs, interfaces (ports), validators
o	Infrastructure — EF Core MySQL, repository implementations, external API clients
o	API — ASP.NET Core Web API (controllers, DI, middleware)
Project reference flow:
API → Application → Domain
API → Infrastructure → Application → Domain
Infrastructure → Application → Domain
________________________________________
2. Domain Layer
•	Base Entity and ValueObject classes.
•	Entities:
o	User (Username, Email)
o	Recipe (Title, Instructions, optional OwnerId)
________________________________________
3. Application Layer
•	Result type (Result & Result<T>) for clean operation results.
•	Repository interfaces (IUserRepository, IRecipeRepository).
•	Base FluentValidation validator (BaseValidator<T> using ClassLevelCascadeMode).
•	DependencyInjection extension method for Application layer (MediatR registration).
________________________________________
4. Infrastructure Layer
•	EF Core DbContext (RecipeAppDbContext) with DbSets for Users and Recipes.
•	Entity configurations in OnModelCreating.
•	Repository implementations (UserRepository, RecipeRepository).
•	DependencyInjection extension method:
o	Configures EF Core MySQL with Pomelo.EntityFrameworkCore.MySql.
o	Registers repositories in DI.
________________________________________
5. API Layer
•	Updated Program.cs:
o	Added AddApplication() and AddInfrastructure().
o	Added controllers, Swagger, health checks.
•	Test Controller:
o	POST /api/test/user — Create a user.
o	GET /api/test/user/{id} — Retrieve a user.
________________________________________
6. Database
•	Installed EF Core tools and design packages.
•	Ran:
•	dotnet ef migrations add InitialCreate \
•	  --project src/RecipeApp.Infrastructure \
•	  --startup-project src/RecipeApp.API
•	
•	dotnet ef database update \
•	  --project src/RecipeApp.Infrastructure \
•	  --startup-project src/RecipeApp.API
•	MySQL database created with Users and Recipes tables.
________________________________________
📊 Status after Phase 2
•	Backend compiles & runs.
•	Database schema is created and in sync.
•	Swagger available with test endpoints.
•	Ready to start Phase 3 (TheMealDB API integration).

📄 Project Documentation – Phase 3
Public Recipe Fetching from TheMealDB
________________________________________
Objective
Enable the backend to fetch public recipe data from the external TheMealDB API, transform it into our domain-friendly DTOs, and expose it via a clean architecture endpoint.
________________________________________
Changes in This Phase
1. Application Layer
•	Created DTOs
📂 src/RecipeApp.Application/Recipes/Dtos/MealDto.cs
•	public record MealDto(
•	    string Id,
•	    string Name,
•	    string Category,
•	    string Instructions,
•	    string ThumbnailUrl,
•	    string YoutubeUrl,
•	    List<MealIngredient> Ingredients
•	);
•	
•	public record MealIngredient(
•	    string Name,
•	    string Measure
•	);
Purpose: Provide a clean structure for recipe details, including ingredients, measures, and a YouTube link.
•	Created TheMealDB Query
📂 Recipes/Queries/GetPublicRecipes
o	GetPublicRecipesQuery → carries search term.
o	GetPublicRecipesQueryHandler → calls ITheMealDbService to fetch and map results.
•	Added Interface
📂 Application/Common/Interfaces/ITheMealDbService.cs
•	public interface ITheMealDbService
•	{
•	    Task<IEnumerable<MealDto>> SearchMealsAsync(string search, CancellationToken cancellationToken = default);
•	}
________________________________________
2. Infrastructure Layer
•	Installed required packages
•	dotnet add src/RecipeApp.Infrastructure package Microsoft.Extensions.Http
•	Implemented Service
📂 Infrastructure/Services/TheMealDbService.cs
o	Uses HttpClient to call TheMealDB API.
o	Maps up to 20 ingredients + measures into a list of MealIngredient.
o	Extracts the YouTube link from strYoutube.
o	Base URL configured in appsettings.json:
o	"TheMealDb": {
o	  "BaseUrl": "https://www.themealdb.com/api/json/v1/1/"
o	}
________________________________________
3. API Layer
•	Added Controller
📂 API/Controllers/PublicRecipesController.cs
•	[HttpGet]
•	public async Task<IActionResult> Get([FromQuery] string search)
•	{
•	    var recipes = await _mediator.Send(new GetPublicRecipesQuery(search));
•	    return Ok(recipes);
•	}
•	Endpoint:
•	GET /api/publicrecipes?search={keyword}
________________________________________
4. Testing
•	Run
•	dotnet run --project src/RecipeApp.API
•	Swagger
•	http://localhost:5076/swagger
Select GET /api/publicrecipes, enter search term (e.g., chicken), execute.
•	Response Example
•	[
•	  {
•	    "id": "52772",
•	    "name": "Teriyaki Chicken Casserole",
•	    "category": "Chicken",
•	    "instructions": "...",
•	    "thumbnailUrl": "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg",
•	    "youtubeUrl": "https://www.youtube.com/watch?v=4aZr5hZXP_s",
•	    "ingredients": [
•	      { "name": "soy sauce", "measure": "3/4 cup" },
•	      { "name": "water", "measure": "1/2 cup" }
•	    ]
•	  }
•	]
________________________________________
Key Points
•	API calls are abstracted via ITheMealDbService → Infrastructure handles HTTP details.
•	Ingredients and measures are flattened from TheMealDB’s numbered fields.
•	Result model (MealDto) is framework-agnostic and safe for frontend consumption.
•	Swagger allows easy manual testing.


