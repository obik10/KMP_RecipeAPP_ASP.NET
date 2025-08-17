using RecipeApp.Domain.Common;

namespace RecipeApp.Domain.Entities;

public class FavoriteRecipe : Entity
{
    public Guid UserId { get; private set; }
    public Guid RecipeId { get; private set; }

    // Navigation properties (optional)
    public Recipe? Recipe { get; private set; }

    private FavoriteRecipe() { } // EF

    public FavoriteRecipe(Guid userId, Guid recipeId)
    {
        UserId = userId;
        RecipeId = recipeId;
    }
}
