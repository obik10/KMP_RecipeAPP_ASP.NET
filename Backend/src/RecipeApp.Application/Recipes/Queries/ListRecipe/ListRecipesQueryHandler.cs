using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;

namespace RecipeApp.Application.Recipes.Queries.ListRecipes;

public class ListRecipesQueryHandler : IRequestHandler<ListRecipesQuery, IEnumerable<RecipeDto>>
{
    private readonly IRecipeRepository _repo;

    public ListRecipesQueryHandler(IRecipeRepository repo)
    {
        _repo = repo;
    }

    public async Task<IEnumerable<RecipeDto>> Handle(ListRecipesQuery request, CancellationToken cancellationToken)
    {
        var entities = await _repo.GetAllWithIngredientsAsync(cancellationToken);
        return entities.Select(e => e.ToDto());
    }
}
