using RecipeApp.Domain.Common;

namespace RecipeApp.Domain.Entities;

public class RecipeIngredient : Entity
{
    public string Name { get; private set; } = default!;
    public string Measure { get; private set; } = default!;
    public Guid RecipeId { get; private set; }

    // EF
    private RecipeIngredient() { }

    public RecipeIngredient(string name, string measure)
    {
        Name = name;
        Measure = measure;
    }

    public void Update(string name, string measure)
    {
        Name = name;
        Measure = measure;
    }
}
