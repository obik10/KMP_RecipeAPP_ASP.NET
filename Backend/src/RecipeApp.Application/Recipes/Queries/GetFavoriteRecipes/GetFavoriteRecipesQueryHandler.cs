using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;

namespace RecipeApp.Application.Recipes.Queries.GetFavoriteRecipes;

public class GetFavoriteRecipesQueryHandler : IRequestHandler<GetFavoriteRecipesQuery, IEnumerable<RecipeDto>>
{
    private readonly IFavoriteRepository _repo;

    public GetFavoriteRecipesQueryHandler(IFavoriteRepository repo)
    {
        _repo = repo;
    }

    public async Task<IEnumerable<RecipeDto>> Handle(GetFavoriteRecipesQuery request, CancellationToken cancellationToken)
    {
        var favs = await _repo.GetFavoritesByUserAsync(request.UserId, cancellationToken);
        return favs.Select(r => r.ToDto()).ToList();
    }
}
