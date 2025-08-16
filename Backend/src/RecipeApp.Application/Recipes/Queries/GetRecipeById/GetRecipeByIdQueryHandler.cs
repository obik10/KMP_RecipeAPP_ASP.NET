using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;

namespace RecipeApp.Application.Recipes.Queries.GetRecipeById;

public class GetRecipeByIdQueryHandler : IRequestHandler<GetRecipeByIdQuery, RecipeDto?>
{
    private readonly IRecipeRepository _repo;

    public GetRecipeByIdQueryHandler(IRecipeRepository repo)
    {
        _repo = repo;
    }

    public async Task<RecipeDto?> Handle(GetRecipeByIdQuery request, CancellationToken cancellationToken)
    {
        var entity = await _repo.GetByIdWithIngredientsAsync(request.Id, cancellationToken);
        return entity?.ToDto();
    }
}
