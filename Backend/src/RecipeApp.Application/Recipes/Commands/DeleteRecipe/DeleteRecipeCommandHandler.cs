using MediatR;
using RecipeApp.Application.Common.Interfaces;

namespace RecipeApp.Application.Recipes.Commands.DeleteRecipe;

public class DeleteRecipeCommandHandler : IRequestHandler<DeleteRecipeCommand, Unit>
{
    private readonly IRecipeRepository _repo;

    public DeleteRecipeCommandHandler(IRecipeRepository repo)
    {
        _repo = repo;
    }

    public async Task<Unit> Handle(DeleteRecipeCommand request, CancellationToken cancellationToken)
    {
        await _repo.DeleteAsync(request.Id, cancellationToken);
        return Unit.Value;
    }
}
