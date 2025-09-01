using System.Collections.Generic;
using System.Linq;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Domain.Entities;

namespace RecipeApp.Application.Recipes.Mappings;

public static class RecipeMappings
{
    public static RecipeIngredientDto ToDto(this RecipeIngredient ingredient)
        => new RecipeIngredientDto(
            ingredient.Name,
            ingredient.Measure
        );

    public static RecipeDto ToDto(this Recipe recipe)
        => new RecipeDto(
            recipe.Id,
            recipe.IsExternal,
            recipe.Title,
            recipe.Instructions,
            recipe.OwnerId,
            recipe.ImagePath,
            (recipe.Ingredients ?? new List<RecipeIngredient>())
                .Select(i => i.ToDto())
                .ToList(),
            recipe.YoutubeUrl
        );
}
