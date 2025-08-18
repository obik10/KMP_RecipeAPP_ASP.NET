using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Domain.Entities;

namespace RecipeApp.Application.Recipes.Mappings;

public static class RecipeMappings
{
    public static RecipeDto ToDto(this Recipe entity) =>
        new(
            entity.Id,
            entity.IsExternal,
            entity.Title,
            entity.Instructions,
            entity.OwnerId,
            entity.ImagePath,
            entity.Ingredients
                  .Select(i => new RecipeIngredientDto(i.Name, i.Measure))
                  .ToList(),
            entity.YoutubeUrl
            
        );
}
