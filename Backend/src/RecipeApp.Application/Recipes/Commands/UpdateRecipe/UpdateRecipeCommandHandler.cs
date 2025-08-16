using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;

namespace RecipeApp.Application.Recipes.Commands.UpdateRecipe;

public class UpdateRecipeCommandHandler : IRequestHandler<UpdateRecipeCommand, RecipeDto>
{
    private readonly IRecipeRepository _repo;

    public UpdateRecipeCommandHandler(IRecipeRepository repo)
    {
        _repo = repo;
    }

    public async Task<RecipeDto> Handle(UpdateRecipeCommand request, CancellationToken cancellationToken)
    {
        var entity = await _repo.GetByIdWithIngredientsAsync(request.Id, cancellationToken)
                     ?? throw new KeyNotFoundException("Recipe not found.");

        // update basic data
        entity.Update(request.Title, request.Instructions);

        // replace all ingredients
        entity.ReplaceIngredients(request.Ingredients.Select(i => (i.Name, i.Measure)));

        await _repo.UpdateAsync(entity, cancellationToken);

        return entity.ToDto();
    }
}
