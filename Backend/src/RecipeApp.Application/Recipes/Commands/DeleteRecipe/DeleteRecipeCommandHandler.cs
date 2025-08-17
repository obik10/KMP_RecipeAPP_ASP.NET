using MediatR;
using RecipeApp.Application.Common.Interfaces;

namespace RecipeApp.Application.Recipes.Commands.DeleteRecipe;

public class DeleteRecipeCommandHandler : IRequestHandler<DeleteRecipeCommand, Unit>
{
    private readonly IRecipeRepository _repo;
    private readonly ICurrentUserService _currentUser;

    public DeleteRecipeCommandHandler(IRecipeRepository repo, ICurrentUserService currentUser)
    {
        _repo = repo;
        _currentUser = currentUser;
    }

    public async Task<Unit> Handle(DeleteRecipeCommand request, CancellationToken cancellationToken)
    {
        var entity = await _repo.GetByIdAsync(request.Id, cancellationToken)
                     ?? throw new KeyNotFoundException("Recipe not found.");

        if (entity.OwnerId != _currentUser.UserId)
            throw new UnauthorizedAccessException("You are not allowed to delete this recipe.");

        await _repo.DeleteAsync(request.Id, cancellationToken);
        return Unit.Value;
    }
}
