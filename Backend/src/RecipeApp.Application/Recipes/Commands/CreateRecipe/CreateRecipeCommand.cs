using MediatR;
using RecipeApp.Application.Recipes.Commands.Shared;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Commands.CreateRecipe;

public record CreateRecipeCommand(
    string Title,
    string Instructions,
    Guid? OwnerId,
    List<RecipeIngredientInput> Ingredients,
    string? YoutubeUrl = null // optional YouTube link
) : IRequest<RecipeDto>;
