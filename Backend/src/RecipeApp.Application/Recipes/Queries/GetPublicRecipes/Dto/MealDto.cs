namespace RecipeApp.Application.Recipes.Dtos;

public record MealDto(
    string Id,
    string Name,
    string Category,
    string Instructions,
    string ThumbnailUrl,
    string YoutubeUrl,
    List<MealIngredient> Ingredients
);

public record MealIngredient(
    string Name,
    string Measure
);
