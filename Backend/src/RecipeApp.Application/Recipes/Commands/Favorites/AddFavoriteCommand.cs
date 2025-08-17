using MediatR;

namespace RecipeApp.Application.Recipes.Commands.Favorites;

public record AddFavoriteCommand(Guid UserId, Guid RecipeId) : IRequest<Unit>;
