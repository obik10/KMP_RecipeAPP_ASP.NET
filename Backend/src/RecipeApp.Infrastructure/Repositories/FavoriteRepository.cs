using Microsoft.EntityFrameworkCore;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Domain.Entities;

namespace RecipeApp.Infrastructure.Persistence.Repositories;

public class FavoriteRepository : IFavoriteRepository
{
    private readonly RecipeAppDbContext _db;

    public FavoriteRepository(RecipeAppDbContext db)
    {
        _db = db;
    }

    public async Task<IEnumerable<Recipe>> GetFavoritesByUserAsync(Guid userId, CancellationToken cancellationToken = default)
    {
        return await _db.FavoriteRecipes
        .Where(f => f.UserId == userId)
        .Include(f => f.Recipe!)
            .ThenInclude(r => r.Ingredients)
        .Select(f => f.Recipe!)
        .Where(r => r != null)
        .ToListAsync(cancellationToken);
    }

    public async Task AddAsync(FavoriteRecipe favorite, CancellationToken cancellationToken = default)
    {
        _db.FavoriteRecipes.Add(favorite);
        await _db.SaveChangesAsync(cancellationToken);
    }

    public async Task RemoveAsync(Guid userId, Guid recipeId, CancellationToken cancellationToken = default)
    {
        var fav = await _db.FavoriteRecipes
            .FirstOrDefaultAsync(f => f.UserId == userId && f.RecipeId == recipeId, cancellationToken);

        if (fav != null)
        {
            _db.FavoriteRecipes.Remove(fav);
            await _db.SaveChangesAsync(cancellationToken);
        }
    }

    public async Task<bool> ExistsAsync(Guid userId, Guid recipeId, CancellationToken cancellationToken = default)
    {
        return await _db.FavoriteRecipes.AnyAsync(f => f.UserId == userId && f.RecipeId == recipeId, cancellationToken);
    }
}
