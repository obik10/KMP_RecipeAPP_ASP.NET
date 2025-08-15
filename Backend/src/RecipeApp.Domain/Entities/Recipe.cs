using RecipeApp.Domain.Common;

namespace RecipeApp.Domain.Entities;

public class Recipe : Entity
{
    public string Title { get; private set; }
    public string Instructions { get; private set; }
    public Guid? OwnerId { get; private set; }

    // EF Core / serialization
    private Recipe() { }

    public Recipe(string title, string instructions, Guid? ownerId = null)
    {
        Title = title;
        Instructions = instructions;
        OwnerId = ownerId;
    }
}
