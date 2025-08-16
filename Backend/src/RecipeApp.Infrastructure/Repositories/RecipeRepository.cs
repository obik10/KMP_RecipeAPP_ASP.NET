using Microsoft.EntityFrameworkCore;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Domain.Entities;
using RecipeApp.Infrastructure.Persistence;

namespace RecipeApp.Infrastructure.Repositories;

public class RecipeRepository : IRecipeRepository
{
    private readonly RecipeAppDbContext _context;

    public RecipeRepository(RecipeAppDbContext context)
    {
        _context = context;
    }

    public async Task<Recipe?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        return await _context.Recipes.FindAsync(new object?[] { id }, cancellationToken);
    }

    public async Task<Recipe?> GetByIdWithIngredientsAsync(Guid id, CancellationToken cancellationToken = default)
    {
        return await _context.Recipes
            .Include(r => r.Ingredients)
            .FirstOrDefaultAsync(r => r.Id == id, cancellationToken);
    }

    public async Task<IEnumerable<Recipe>> GetAllWithIngredientsAsync(CancellationToken cancellationToken = default)
    {
        return await _context.Recipes
            .Include(r => r.Ingredients)
            .ToListAsync(cancellationToken);
    }

    public async Task AddAsync(Recipe recipe, CancellationToken cancellationToken = default)
    {
        await _context.Recipes.AddAsync(recipe, cancellationToken);
        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task UpdateAsync(Recipe recipe, CancellationToken cancellationToken = default)
    {
        _context.Recipes.Update(recipe);
        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task DeleteAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var recipe = await _context.Recipes.FindAsync(new object?[] { id }, cancellationToken);
        if (recipe != null)
        {
            _context.Recipes.Remove(recipe);
            await _context.SaveChangesAsync(cancellationToken);
        }
    }

    public async Task<IEnumerable<Recipe>> SearchAsync(string keyword, CancellationToken cancellationToken = default)
{
    return await _context.Recipes
        .Include(r => r.Ingredients)
        .Where(r =>
            r.Title.Contains(keyword) ||
            r.Instructions.Contains(keyword) ||
            r.Ingredients.Any(i => i.Name.Contains(keyword)))
        .ToListAsync(cancellationToken);
}
}
