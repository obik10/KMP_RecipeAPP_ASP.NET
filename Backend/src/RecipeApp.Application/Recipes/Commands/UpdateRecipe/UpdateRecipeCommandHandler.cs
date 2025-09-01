using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;
using RecipeApp.Domain.Entities;

namespace RecipeApp.Application.Recipes.Commands.UpdateRecipe;

public class UpdateRecipeCommandHandler : IRequestHandler<UpdateRecipeCommand, RecipeDto>
{
    private readonly IRecipeRepository _repository;
    private readonly ICurrentUserService _currentUser;

    public UpdateRecipeCommandHandler(IRecipeRepository repository, ICurrentUserService currentUser)
    {
        _repository = repository;
        _currentUser = currentUser;
    }

    public async Task<RecipeDto> Handle(UpdateRecipeCommand request, CancellationToken cancellationToken)
    {
        // Load recipe with ingredients
        var recipe = await _repository.GetByIdWithIngredientsAsync(request.Id, cancellationToken);
        
        if (recipe == null)
            throw new KeyNotFoundException($"Recipe with ID {request.Id} not found.");

        // Authorization check
        if (recipe.OwnerId != _currentUser.UserId)
            throw new UnauthorizedAccessException("You are not allowed to update this recipe.");

        // Update the existing recipe's properties
        recipe.Update(request.Title, request.Instructions, request.YoutubeUrl);

        // Persist changes with ingredients
        await _repository.UpdateAsync(recipe, request.Ingredients.Select(i => (i.Name, i.Measure)), cancellationToken);

        // Reload to ensure proper mapping
        var updatedRecipe = await _repository.GetByIdWithIngredientsAsync(request.Id, cancellationToken)
                            ?? throw new KeyNotFoundException("Recipe not found after update.");

        return updatedRecipe.ToDto();
    }
}
