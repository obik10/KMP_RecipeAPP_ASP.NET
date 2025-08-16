using MediatR;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Queries.SearchRecipes;

public record SearchRecipesQuery(string Keyword) : IRequest<IEnumerable<RecipeDto>>;
