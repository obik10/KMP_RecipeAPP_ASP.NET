using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Common.Models;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;

namespace RecipeApp.Application.Recipes.Queries.ListRecipes;

public class ListRecipesQueryHandler : IRequestHandler<ListRecipesQuery, PaginatedResult<RecipeDto>>
{
    private readonly IRecipeRepository _repo;

    public ListRecipesQueryHandler(IRecipeRepository repo)
    {
        _repo = repo;
    }

    public async Task<PaginatedResult<RecipeDto>> Handle(ListRecipesQuery request, CancellationToken cancellationToken)
    {
        // Get total count
        var totalCount = await _repo.CountAsync(cancellationToken);

        // Fetch paginated items
        var entities = await _repo.GetPagedWithIngredientsAsync(
            request.PageNumber,
            request.PageSize,
            cancellationToken);

        var items = entities.Select(e => e.ToDto());

        return new PaginatedResult<RecipeDto>(
            items,
            totalCount,
            request.PageNumber,
            request.PageSize
        );
    }
}
