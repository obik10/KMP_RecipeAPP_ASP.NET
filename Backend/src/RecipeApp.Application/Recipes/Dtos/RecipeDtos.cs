namespace RecipeApp.Application.Recipes.Dtos;

public record RecipeIngredientDto(string Name, string Measure);

public record RecipeDto(
    Guid Id,
    bool IsExternal,
    string Title,
    string Instructions,
    Guid? OwnerId,
    string? ImagePath, // relative URL, e.g., "/uploads/xyz.jpg"
    List<RecipeIngredientDto> Ingredients
);