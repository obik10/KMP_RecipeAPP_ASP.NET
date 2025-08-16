namespace RecipeApp.Application.Common.Interfaces;

public interface IFileStorage
{
    /// <summary>Saves a file stream and returns a relative web path, e.g., "/uploads/123.jpg".</summary>
    Task<string> SaveAsync(Stream content, string fileName, string contentType, CancellationToken ct = default);

    /// <summary>Deletes a previously saved file path (relative path).</summary>
    Task DeleteAsync(string relativePath, CancellationToken ct = default);
}
