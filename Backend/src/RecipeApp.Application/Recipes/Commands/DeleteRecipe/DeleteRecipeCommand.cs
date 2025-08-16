using MediatR;

namespace RecipeApp.Application.Recipes.Commands.DeleteRecipe;

public record DeleteRecipeCommand(Guid Id) : IRequest<Unit>;
