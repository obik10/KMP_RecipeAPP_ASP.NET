using MediatR;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Queries.ListRecipes;

public record ListRecipesQuery() : IRequest<IEnumerable<RecipeDto>>;
