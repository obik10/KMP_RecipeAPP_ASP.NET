using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;

namespace RecipeApp.Application.Recipes.Commands.UpdateRecipe;

public class UpdateRecipeCommandHandler : IRequestHandler<UpdateRecipeCommand, RecipeDto>
{
    private readonly IRecipeRepository _repo;
    private readonly ICurrentUserService _currentUser;

    public UpdateRecipeCommandHandler(IRecipeRepository repo, ICurrentUserService currentUser)
    {
        _repo = repo;
        _currentUser = currentUser;
    }

    public async Task<RecipeDto> Handle(UpdateRecipeCommand request, CancellationToken cancellationToken)
    {
        var entity = await _repo.GetByIdWithIngredientsAsync(request.Id, cancellationToken)
                     ?? throw new KeyNotFoundException("Recipe not found.");

        if (entity.OwnerId != _currentUser.UserId)
            throw new UnauthorizedAccessException("You are not allowed to update this recipe.");

        entity.Update(request.Title, request.Instructions, request.YoutubeUrl);
        entity.ReplaceIngredients(request.Ingredients.Select(i => (i.Name, i.Measure)));

        await _repo.UpdateAsync(entity, cancellationToken);
        return entity.ToDto();
    }
}
