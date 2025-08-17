using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Queries.SearchRecipes;

public class SearchRecipesQueryHandler : IRequestHandler<SearchRecipesQuery, IEnumerable<RecipeDto>>
{
    private readonly IRecipeRepository _repository;

    public SearchRecipesQueryHandler(IRecipeRepository repository)
    {
        _repository = repository;
    }

    public async Task<IEnumerable<RecipeDto>> Handle(SearchRecipesQuery request, CancellationToken cancellationToken)
    {
        var recipes = await _repository.SearchAsync(request.Keyword, cancellationToken);

        return recipes.Select(r => new RecipeDto(
            r.Id,
            r.IsExternal,
            r.Title,
            r.Instructions,
            r.OwnerId,
            r.ImagePath,
            r.Ingredients.Select(i => new RecipeIngredientDto(i.Name, i.Measure)).ToList()
            
        ));
    }
}
