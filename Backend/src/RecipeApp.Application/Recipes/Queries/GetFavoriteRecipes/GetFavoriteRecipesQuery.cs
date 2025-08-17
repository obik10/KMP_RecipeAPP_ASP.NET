using MediatR;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Queries.GetFavoriteRecipes;

public record GetFavoriteRecipesQuery(Guid UserId) : IRequest<IEnumerable<RecipeDto>>;
