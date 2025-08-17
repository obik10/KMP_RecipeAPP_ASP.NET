using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;

namespace RecipeApp.Application.Recipes.Queries.GetMyRecipes;

public class GetMyRecipesQueryHandler : IRequestHandler<GetMyRecipesQuery, IEnumerable<RecipeDto>>
{
    private readonly IRecipeRepository _repo;

    public GetMyRecipesQueryHandler(IRecipeRepository repo)
    {
        _repo = repo;
    }

    public async Task<IEnumerable<RecipeDto>> Handle(GetMyRecipesQuery request, CancellationToken cancellationToken)
    {
        var all = await _repo.GetAllWithIngredientsAsync(cancellationToken);
        return all
            .Where(r => r.OwnerId == request.UserId)
            .Select(r => r.ToDto())
            .ToList();
    }
}
