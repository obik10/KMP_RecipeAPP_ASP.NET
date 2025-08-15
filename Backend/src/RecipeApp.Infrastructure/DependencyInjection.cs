using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Infrastructure.Persistence;
using RecipeApp.Infrastructure.Repositories;
using RecipeApp.Infrastructure.Services;

namespace RecipeApp.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        services.AddHttpClient<ITheMealDbService, TheMealDbService>();

        var connectionString = configuration.GetConnectionString("DefaultConnection");

        services.AddDbContext<RecipeAppDbContext>(options =>
            options.UseMySql(connectionString, ServerVersion.AutoDetect(connectionString)));

        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IRecipeRepository, RecipeRepository>();

        // TheMealDB API client
        services.AddHttpClient<ITheMealDbService, TheMealDbService>();

        return services;
    }
}
