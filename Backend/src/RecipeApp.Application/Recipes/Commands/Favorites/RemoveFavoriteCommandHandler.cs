using MediatR;
using RecipeApp.Application.Common.Interfaces;

namespace RecipeApp.Application.Recipes.Commands.Favorites;

public class RemoveFavoriteCommandHandler : IRequestHandler<RemoveFavoriteCommand, Unit>
{
    private readonly IFavoriteRepository _repo;

    public RemoveFavoriteCommandHandler(IFavoriteRepository repo)
    {
        _repo = repo;
    }

    public async Task<Unit> Handle(RemoveFavoriteCommand request, CancellationToken cancellationToken)
    {
        await _repo.RemoveAsync(request.UserId, request.RecipeId, cancellationToken);
        return Unit.Value;
    }
}
