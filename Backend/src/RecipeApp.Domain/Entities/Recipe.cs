using RecipeApp.Domain.Common;

namespace RecipeApp.Domain.Entities;

public class Recipe : Entity
{
    // Core properties
    public string Title { get; private set; } = default!;
    public string Instructions { get; private set; } = default!;
    public Guid? OwnerId { get; private set; } // Optional owner
    public string? YoutubeUrl { get; private set; }
    public string? ImagePath { get; private set; }

    // Ingredients
    private readonly List<RecipeIngredient> _ingredients = new();
    public IReadOnlyCollection<RecipeIngredient> Ingredients => _ingredients.AsReadOnly();

    // External source metadata
    public bool IsExternal { get; private set; }
    public string? ExternalSource { get; private set; }
    public string? ExternalId { get; private set; }

    // EF Core constructor
    private Recipe() { }

    // Constructor for new recipe
    public Recipe(string title, string instructions, Guid? ownerId = null, string? youtubeUrl = null)
    {
        Title = title;
        Instructions = instructions;
        OwnerId = ownerId;

        if (!IsValidYoutubeUrl(youtubeUrl))
            throw new ArgumentException("Invalid YouTube URL", nameof(youtubeUrl));

        YoutubeUrl = string.IsNullOrWhiteSpace(youtubeUrl) ? null : youtubeUrl;
    }

    // Update main properties
    public void Update(string title, string instructions, string? youtubeUrl = null)
    {
        Title = title;
        Instructions = instructions;

        if (!IsValidYoutubeUrl(youtubeUrl))
            throw new ArgumentException("Invalid YouTube URL", nameof(youtubeUrl));

        YoutubeUrl = string.IsNullOrWhiteSpace(youtubeUrl) ? null : youtubeUrl;
    }

    public void SetImagePath(string? imagePath) => ImagePath = imagePath;

    // Replace ingredients (simple)
    public void ReplaceIngredients(IEnumerable<(string Name, string Measure)> items)
    {
        _ingredients.Clear();
        foreach (var (name, measure) in items)
        {
            _ingredients.Add(new RecipeIngredient(name, measure, this.Id));
        }
    }

    // Replace ingredients (advanced: supports updates by Id)
    public void ReplaceIngredients(IEnumerable<(Guid? Id, string Name, string Measure)> items)
    {
        var incoming = items?.ToList() ?? new List<(Guid? Id, string Name, string Measure)>();

        // Map incoming items with Id
        var incomingById = incoming
            .Where(x => x.Id.HasValue && x.Id.Value != Guid.Empty)
            .ToDictionary(x => x.Id!.Value, x => (x.Name, x.Measure));

        // 1️⃣ Remove ingredients not in incoming list
        for (int i = _ingredients.Count - 1; i >= 0; i--)
        {
            if (!incomingById.ContainsKey(_ingredients[i].Id))
                _ingredients.RemoveAt(i);
        }

        // 2️⃣ Update existing ingredients
        foreach (var existing in _ingredients)
        {
            if (incomingById.TryGetValue(existing.Id, out var inc))
                existing.Update(inc.Name, inc.Measure);
        }

        // 3️⃣ Add new ingredients (without Id)
        foreach (var newItem in incoming.Where(x => !x.Id.HasValue || x.Id == Guid.Empty))
        {
            _ingredients.Add(new RecipeIngredient(newItem.Name, newItem.Measure, this.Id));
        }

        // 4️⃣ Add unmatched Ids (if client sent unknown Id)
        foreach (var unmatched in incoming.Where(x => x.Id.HasValue && x.Id != Guid.Empty && !_ingredients.Any(e => e.Id == x.Id.Value)))
        {
            _ingredients.Add(new RecipeIngredient(unmatched.Name, unmatched.Measure, this.Id));
        }
    }

    // Mark recipe as external
    public void MarkAsExternal(string source, string externalId, string? youtubeUrl = null)
    {
        IsExternal = true;
        ExternalSource = source;
        ExternalId = externalId;
        YoutubeUrl = youtubeUrl;
    }

    // Youtube URL validator
    private static bool IsValidYoutubeUrl(string? url)
    {
        if (string.IsNullOrWhiteSpace(url))
            return true;

        if (!Uri.TryCreate(url, UriKind.Absolute, out var uri))
            return false;

        return uri.Host.Contains("youtube.com", StringComparison.OrdinalIgnoreCase) ||
               uri.Host.Contains("youtu.be", StringComparison.OrdinalIgnoreCase);
    }
}
