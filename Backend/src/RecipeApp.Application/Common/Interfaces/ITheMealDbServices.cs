using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Application.Common.Interfaces;

public interface ITheMealDbService
{
    Task<IEnumerable<MealDto>> SearchMealsAsync(string search, CancellationToken cancellationToken = default);
}
