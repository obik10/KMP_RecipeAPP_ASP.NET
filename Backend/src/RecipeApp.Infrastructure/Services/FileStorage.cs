using RecipeApp.Application.Common.Interfaces;

namespace RecipeApp.Infrastructure.Services;

public class FileStorage : IFileStorage
{
    private readonly string _rootPath;

    public FileStorage(string rootPath)
    {
        _rootPath = rootPath;
    }

    public async Task<string> SaveAsync(Stream content, string fileName, string contentType, CancellationToken ct = default)
    {
        var uploadsDir = Path.Combine(_rootPath, "uploads");
        if (!Directory.Exists(uploadsDir))
        {
            Directory.CreateDirectory(uploadsDir);
        }

        var uniqueName = $"{Guid.NewGuid()}{Path.GetExtension(fileName)}";
        var filePath = Path.Combine(uploadsDir, uniqueName);

        using (var fileStream = new FileStream(filePath, FileMode.Create, FileAccess.Write))
        {
            await content.CopyToAsync(fileStream, ct);
        }

        // Return relative path for serving via API
        return $"/uploads/{uniqueName}";
    }

    public Task DeleteAsync(string relativePath, CancellationToken ct = default)
    {
        var filePath = Path.Combine(_rootPath, relativePath.TrimStart('/'));
        if (File.Exists(filePath))
        {
            File.Delete(filePath);
        }
        return Task.CompletedTask;
    }
}
