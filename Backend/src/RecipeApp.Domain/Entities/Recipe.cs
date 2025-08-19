using RecipeApp.Domain.Common;

namespace RecipeApp.Domain.Entities;

public class Recipe : Entity
{
    public string Title { get; private set; } = default!;
    public string Instructions { get; private set; } = default!;

    public string? YoutubeUrl { get; private set; }

    // Now REQUIRED (non-nullable)
    public Guid? OwnerId { get; private set; }

    // Image path relative to web root (e.g., "/uploads/abcd.jpg")
    public string? ImagePath { get; private set; }

    private readonly List<RecipeIngredient> _ingredients = new();
    public IReadOnlyList<RecipeIngredient> Ingredients => _ingredients;

    // External source (e.g., TheMealDB)
    public bool IsExternal { get; private set; }
    public string? ExternalSource { get; private set; }
    public string? ExternalId { get; private set; }

    // EF
    private Recipe() { }
    
    private static bool IsValidYoutubeUrl(string? url)
{
    if (string.IsNullOrWhiteSpace(url))
        return true; // null or empty is allowed

    // Try to parse as Uri
    if (!Uri.TryCreate(url, UriKind.Absolute, out var uri))
        return false;

    // Must be youtube.com or youtu.be
    return uri.Host.Contains("youtube.com", StringComparison.OrdinalIgnoreCase) ||
           uri.Host.Contains("youtu.be", StringComparison.OrdinalIgnoreCase);
}

public Recipe(string title, string instructions, Guid? ownerId = null, string? youtubeUrl = null)
    {
        Title = title;
        Instructions = instructions;
        OwnerId = ownerId;
        if (!IsValidYoutubeUrl(youtubeUrl))
        throw new ArgumentException("Invalid YouTube URL", nameof(youtubeUrl));

    YoutubeUrl = string.IsNullOrWhiteSpace(youtubeUrl) ? null : youtubeUrl;
    }

public void Update(string title, string instructions, string? youtubeUrl = null)
{
    Title = title;
    Instructions = instructions;
    
   if (!IsValidYoutubeUrl(youtubeUrl))
            throw new ArgumentException("Invalid YouTube URL", nameof(youtubeUrl));

    YoutubeUrl = string.IsNullOrWhiteSpace(youtubeUrl) ? null : youtubeUrl;
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

 public void MarkAsExternal(string source, string externalId, string? youtubeUrl = null)
{
    IsExternal = true;
    ExternalSource = source;
    ExternalId = externalId;
    YoutubeUrl = youtubeUrl;
}

}
