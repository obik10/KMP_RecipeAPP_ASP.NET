using FluentValidation;
using RecipeApp.Application.Common.Validators;

namespace RecipeApp.Application.Recipes.Commands.UploadRecipeImage;

public class UploadRecipeImageCommandValidator : BaseValidator<UploadRecipeImageCommand>
{
    public UploadRecipeImageCommandValidator()
    {
        RuleFor(x => x.RecipeId).NotEmpty();
        RuleFor(x => x.FileName).NotEmpty().MaximumLength(255);
        RuleFor(x => x.ContentType)
            .NotEmpty()
            .Must(ct => ct.StartsWith("image/"))
            .WithMessage("Only image content types are allowed.");
        // Stream size checking is better done at API layer via FormOptions, but we at least ensure stream is provided
        RuleFor(x => x.Content).NotNull();
    }
}
