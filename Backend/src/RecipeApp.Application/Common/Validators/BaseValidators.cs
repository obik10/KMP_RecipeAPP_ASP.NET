using FluentValidation;

namespace RecipeApp.Application.Common.Validators;

public abstract class BaseValidator<T> : AbstractValidator<T>
{
    protected BaseValidator()
    {
        // Use the new property to stop validation on first failure
        ClassLevelCascadeMode = CascadeMode.Stop;
    }
}
