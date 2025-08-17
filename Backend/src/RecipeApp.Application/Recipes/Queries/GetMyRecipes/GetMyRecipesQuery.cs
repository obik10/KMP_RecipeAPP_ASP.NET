using MediatR;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Queries.GetMyRecipes;

public record GetMyRecipesQuery(Guid UserId) : IRequest<IEnumerable<RecipeDto>>;
