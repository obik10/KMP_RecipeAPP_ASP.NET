using RecipeApp.Domain.Common;

namespace RecipeApp.Domain.Entities;

public class Recipe : Entity
{
    public string Title { get; private set; } = default!;
public string Instructions { get; private set; } = default!;

    public Guid? OwnerId { get; private set; }

    // Image path relative to web root (e.g., "/uploads/abcd.jpg")
    public string? ImagePath { get; private set; }

    private readonly List<RecipeIngredient> _ingredients = new();
    public IReadOnlyList<RecipeIngredient> Ingredients => _ingredients;

    // EF
    private Recipe() { }

    public Recipe(string title, string instructions, Guid? ownerId = null)
    {
        Title = title;
        Instructions = instructions;
        OwnerId = ownerId;
    }

    public void Update(string title, string instructions)
    {
        Title = title;
        Instructions = instructions;
    }

    public void SetImagePath(string? imagePath)
    {
        ImagePath = imagePath;
    }

    public void ReplaceIngredients(IEnumerable<(string Name, string Measure)> items)
    {
        _ingredients.Clear();
        foreach (var (name, measure) in items)
        {
            _ingredients.Add(new RecipeIngredient(name, measure));
        }
    }
}
