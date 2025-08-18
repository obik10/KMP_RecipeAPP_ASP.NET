using FluentValidation;
using RecipeApp.Application.Common.Validators;

namespace RecipeApp.Application.Recipes.Commands.CreateRecipe;

public class CreateRecipeCommandValidator : BaseValidator<CreateRecipeCommand>
{
    public CreateRecipeCommandValidator()
    {
        RuleFor(x => x.Title)
            .NotEmpty()
            .MaximumLength(200);

        RuleFor(x => x.Instructions)
            .NotEmpty();

        RuleForEach(x => x.Ingredients)
            .ChildRules(ing =>
            {
                ing.RuleFor(i => i.Name).NotEmpty().MaximumLength(100);
                ing.RuleFor(i => i.Measure).NotEmpty().MaximumLength(100);
            });

            RuleFor(x => x.YoutubeUrl)
    .Must(url => string.IsNullOrWhiteSpace(url) || Uri.IsWellFormedUriString(url, UriKind.Absolute))
    .WithMessage("YoutubeUrl must be a valid URL if provided.");

    }
}
