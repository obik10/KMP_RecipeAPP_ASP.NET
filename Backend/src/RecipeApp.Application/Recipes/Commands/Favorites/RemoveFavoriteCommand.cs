using MediatR;

namespace RecipeApp.Application.Recipes.Commands.Favorites;

public record RemoveFavoriteCommand(Guid UserId, Guid RecipeId) : IRequest<Unit>;
