using MediatR;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Recipes.Commands.UploadRecipeImage;

public record UploadRecipeImageCommand(
    Guid RecipeId,
    Stream Content,
    string FileName,
    string ContentType
) : IRequest<RecipeDto>;
