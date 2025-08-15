using Microsoft.EntityFrameworkCore;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Domain.Entities;
using RecipeApp.Infrastructure.Persistence;

namespace RecipeApp.Infrastructure.Repositories;

public class RecipeRepository : IRecipeRepository
{
    private readonly RecipeAppDbContext _db;

    public RecipeRepository(RecipeAppDbContext db) => _db = db;

    public async Task<Recipe?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default) =>
        await _db.Recipes.FindAsync(new object[] { id }, cancellationToken);

    public async Task<IEnumerable<Recipe>> GetAllAsync(CancellationToken cancellationToken = default) =>
        await _db.Recipes.AsNoTracking().ToListAsync(cancellationToken);

    public async Task AddAsync(Recipe recipe, CancellationToken cancellationToken = default)
    {
        await _db.Recipes.AddAsync(recipe, cancellationToken);
        await _db.SaveChangesAsync(cancellationToken);
    }

    public async Task DeleteAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var entity = await _db.Recipes.FindAsync(new object[] { id }, cancellationToken);
        if (entity != null)
        {
            _db.Recipes.Remove(entity);
            await _db.SaveChangesAsync(cancellationToken);
        }
    }
}
