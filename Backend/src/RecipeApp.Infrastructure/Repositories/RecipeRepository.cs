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
        var existing = await _context.Recipes
            .Include(r => r.Ingredients)
            .FirstOrDefaultAsync(r => r.Id == recipe.Id, cancellationToken);

        if (existing is null)
            throw new KeyNotFoundException($"Recipe with ID {recipe.Id} not found");

        // Update core properties
        existing.Update(recipe.Title, recipe.Instructions, recipe.YoutubeUrl);
        existing.SetImagePath(recipe.ImagePath);

        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task UpdateAsync(Recipe recipe, IEnumerable<(string Name, string Measure)> ingredients, CancellationToken cancellationToken = default)
    {
        var existing = await _context.Recipes
            .Include(r => r.Ingredients)
            .FirstOrDefaultAsync(r => r.Id == recipe.Id, cancellationToken);

        if (existing is null)
            throw new KeyNotFoundException($"Recipe with ID {recipe.Id} not found in the database.");

        // Update core properties
        existing.Update(recipe.Title, recipe.Instructions, recipe.YoutubeUrl);
        existing.SetImagePath(recipe.ImagePath);

        // Explicitly handle ingredient updates
        // Remove existing ingredients
        _context.RecipeIngredients.RemoveRange(existing.Ingredients);
        
        // Add new ingredients
        foreach (var (name, measure) in ingredients)
        {
            var newIngredient = new RecipeIngredient(name, measure, recipe.Id);
            _context.RecipeIngredients.Add(newIngredient);
        }

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

    // ✅ Pagination support
    public async Task<int> CountAsync(CancellationToken cancellationToken = default)
    {
        return await _context.Recipes.CountAsync(cancellationToken);
    }

    public async Task<IEnumerable<Recipe>> GetPagedWithIngredientsAsync(int pageNumber, int pageSize, CancellationToken cancellationToken = default)
    {
        return await _context.Recipes
            .Include(r => r.Ingredients)
            .OrderBy(r => r.Title) // optional: order by title for consistent results
            .Skip((pageNumber - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync(cancellationToken);
    }
}
