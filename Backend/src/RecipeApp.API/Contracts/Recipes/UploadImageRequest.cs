using Microsoft.AspNetCore.Http;

namespace RecipeApp.API.Contracts.Recipes;

public class UploadImageRequest
{
    public IFormFile File { get; set; } = default!;
}
