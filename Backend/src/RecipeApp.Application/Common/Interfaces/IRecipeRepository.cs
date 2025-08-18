using RecipeApp.Domain.Entities;

namespace RecipeApp.Application.Common.Interfaces;

public interface IRecipeRepository
{
    Task<Recipe?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);
    Task<Recipe?> GetByIdWithIngredientsAsync(Guid id, CancellationToken cancellationToken = default);
    Task<IEnumerable<Recipe>> GetAllWithIngredientsAsync(CancellationToken cancellationToken = default);
    Task AddAsync(Recipe recipe, CancellationToken cancellationToken = default);
    Task UpdateAsync(Recipe recipe, CancellationToken cancellationToken = default);
    Task DeleteAsync(Guid id, CancellationToken cancellationToken = default);

    Task<IEnumerable<Recipe>> SearchAsync(string keyword, CancellationToken cancellationToken = default);

    Task<int> CountAsync(CancellationToken cancellationToken);
    Task<IEnumerable<Recipe>> GetPagedWithIngredientsAsync(int pageNumber, int pageSize, CancellationToken cancellationToken);


}
