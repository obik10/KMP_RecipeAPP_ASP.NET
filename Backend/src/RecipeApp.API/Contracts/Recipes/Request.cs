namespace RecipeApp.API.Contracts.Recipes;

public record IngredientRequest(string Name, string Measure);

public record CreateRecipeRequest(
    string Title,
    string Instructions,
    Guid? OwnerId,
    List<IngredientRequest> Ingredients,
     string? YoutubeUrl = null // optional YouTube link
);

public record UpdateRecipeRequest(
    string Title,
    string Instructions,
    List<IngredientRequest> Ingredients,
     string? YoutubeUrl = null // optional YouTube link
);
