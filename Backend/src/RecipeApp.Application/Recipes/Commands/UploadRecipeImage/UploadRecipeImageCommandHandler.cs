using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;

namespace RecipeApp.Application.Recipes.Commands.UploadRecipeImage;

public class UploadRecipeImageCommandHandler : IRequestHandler<UploadRecipeImageCommand, RecipeDto>
{
    private readonly IRecipeRepository _repo;
    private readonly IFileStorage _fileStorage;

    public UploadRecipeImageCommandHandler(IRecipeRepository repo, IFileStorage fileStorage)
    {
        _repo = repo;
        _fileStorage = fileStorage;
    }

    public async Task<RecipeDto> Handle(UploadRecipeImageCommand request, CancellationToken cancellationToken)
    {
        var entity = await _repo.GetByIdAsync(request.RecipeId, cancellationToken)
                     ?? throw new KeyNotFoundException("Recipe not found.");

        // Optional: delete existing image
        if (!string.IsNullOrWhiteSpace(entity.ImagePath))
        {
            await _fileStorage.DeleteAsync(entity.ImagePath!, cancellationToken);
        }

        var savedPath = await _fileStorage.SaveAsync(request.Content, request.FileName, request.ContentType, cancellationToken);
        entity.SetImagePath(savedPath);

        await _repo.UpdateAsync(entity, cancellationToken);
        return entity.ToDto();
    }
}
