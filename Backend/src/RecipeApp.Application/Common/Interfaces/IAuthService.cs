using RecipeApp.Application.Auth.Contracts;

namespace RecipeApp.Application.Common.Interfaces;

public interface IAuthService
{
    Task<RegisterResponse> RegisterAsync(RegisterRequest request, CancellationToken cancellationToken = default);
}
