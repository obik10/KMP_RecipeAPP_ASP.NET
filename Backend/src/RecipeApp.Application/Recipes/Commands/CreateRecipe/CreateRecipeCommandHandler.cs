using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;
using RecipeApp.Domain.Entities;

namespace RecipeApp.Application.Recipes.Commands.CreateRecipe;

public class CreateRecipeCommandHandler : IRequestHandler<CreateRecipeCommand, RecipeDto>
{
    private readonly IRecipeRepository _repo;

    public CreateRecipeCommandHandler(IRecipeRepository repo)
    {
        _repo = repo;
    }

    public async Task<RecipeDto> Handle(CreateRecipeCommand request, CancellationToken cancellationToken)
    {
        var recipe = new Recipe(request.Title, request.Instructions, request.OwnerId);

        // add ingredients
        recipe.ReplaceIngredients(request.Ingredients.Select(i => (i.Name, i.Measure)));

        await _repo.AddAsync(recipe, cancellationToken);

        return recipe.ToDto();
    }
}
