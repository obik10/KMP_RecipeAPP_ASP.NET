using MediatR;
using RecipeApp.Application.Recipes.Commands.Shared;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Commands.UpdateRecipe;

public record UpdateRecipeCommand(
    Guid Id,
    string Title,
    string Instructions,
    List<RecipeIngredientInput> Ingredients
) : IRequest<RecipeDto>;
