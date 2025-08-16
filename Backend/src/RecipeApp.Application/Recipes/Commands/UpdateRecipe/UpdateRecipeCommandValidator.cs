using FluentValidation;
using RecipeApp.Application.Common.Validators;

namespace RecipeApp.Application.Recipes.Commands.UpdateRecipe;

public class UpdateRecipeCommandValidator : BaseValidator<UpdateRecipeCommand>
{
    public UpdateRecipeCommandValidator()
    {
        RuleFor(x => x.Id).NotEmpty();

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
    }
}
