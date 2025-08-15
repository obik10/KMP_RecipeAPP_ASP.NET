using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Queries.GetPublicRecipes;

public class GetPublicRecipesQueryHandler : IRequestHandler<GetPublicRecipesQuery, IEnumerable<MealDto>>
{
    private readonly ITheMealDbService _theMealDbService;

    public GetPublicRecipesQueryHandler(ITheMealDbService theMealDbService)
    {
        _theMealDbService = theMealDbService;
    }

    public async Task<IEnumerable<MealDto>> Handle(GetPublicRecipesQuery request, CancellationToken cancellationToken)
    {
        return await _theMealDbService.SearchMealsAsync(request.Search, cancellationToken);
    }
}
