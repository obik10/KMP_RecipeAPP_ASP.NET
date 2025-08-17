using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Domain.Entities;

namespace RecipeApp.Application.Recipes.Commands.Favorites;

public class AddFavoriteCommandHandler : IRequestHandler<AddFavoriteCommand, Unit>
{
    private readonly IFavoriteRepository _repo;

    public AddFavoriteCommandHandler(IFavoriteRepository repo)
    {
        _repo = repo;
    }

    public async Task<Unit> Handle(AddFavoriteCommand request, CancellationToken cancellationToken)
    {
        if (!await _repo.ExistsAsync(request.UserId, request.RecipeId, cancellationToken))
        {
            await _repo.AddAsync(new FavoriteRecipe(request.UserId, request.RecipeId), cancellationToken);
        }
        return Unit.Value;
    }
}
