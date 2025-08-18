using MediatR;
using RecipeApp.Application.Common.Models;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Queries.ListRecipes;

public record ListRecipesQuery(int PageNumber = 1, int PageSize = 10) 
    : IRequest<PaginatedResult<RecipeDto>>;
