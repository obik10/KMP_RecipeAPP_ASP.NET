using MediatR;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Mappings;

namespace RecipeApp.Application.Recipes.Commands.UploadRecipeImage;

public class UploadRecipeImageCommandHandler : IRequestHandler<UploadRecipeImageCommand, RecipeDto>
{
    private readonly IRecipeRepository _repo;
    private readonly IFileStorage _fileStorage;
    private readonly ICurrentUserService _currentUser;

    public UploadRecipeImageCommandHandler(IRecipeRepository repo, IFileStorage fileStorage, ICurrentUserService currentUser)
    {
        _repo = repo;
        _fileStorage = fileStorage;
        _currentUser = currentUser;
    }

    public async Task<RecipeDto> Handle(UploadRecipeImageCommand request, CancellationToken cancellationToken)
    {
        var entity = await _repo.GetByIdAsync(request.RecipeId, cancellationToken)
                     ?? throw new KeyNotFoundException("Recipe not found.");

        if (entity.OwnerId != _currentUser.UserId)
            throw new UnauthorizedAccessException("You are not allowed to update this recipe image.");

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
