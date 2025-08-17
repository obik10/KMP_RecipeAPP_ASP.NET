using System.Security.Claims;
using Microsoft.AspNetCore.Http;
using RecipeApp.Application.Common.Interfaces;

namespace RecipeApp.Infrastructure.Services;

public class CurrentUserService : ICurrentUserService
{
    public Guid? UserId { get; }

    public CurrentUserService(IHttpContextAccessor httpContextAccessor)
    {
        var user = httpContextAccessor.HttpContext?.User;

        var sub = user?.FindFirstValue(ClaimTypes.NameIdentifier) 
                  ?? user?.FindFirstValue("sub");

        if (Guid.TryParse(sub, out var parsed))
        {
            UserId = parsed;
        }
    }
}
