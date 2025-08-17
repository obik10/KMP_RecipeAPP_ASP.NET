using RecipeApp.Domain.Entities;

namespace RecipeApp.Application.Common.Interfaces;

public interface IFavoriteRepository
{
    Task<IEnumerable<Recipe>> GetFavoritesByUserAsync(Guid userId, CancellationToken cancellationToken = default);
    Task AddAsync(FavoriteRecipe favorite, CancellationToken cancellationToken = default);
    Task RemoveAsync(Guid userId, Guid recipeId, CancellationToken cancellationToken = default);
    Task<bool> ExistsAsync(Guid userId, Guid recipeId, CancellationToken cancellationToken = default);
}
