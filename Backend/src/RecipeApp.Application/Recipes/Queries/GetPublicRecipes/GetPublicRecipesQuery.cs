using MediatR;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Queries.GetPublicRecipes;

public record GetPublicRecipesQuery(string Search) : IRequest<IEnumerable<MealDto>>;
