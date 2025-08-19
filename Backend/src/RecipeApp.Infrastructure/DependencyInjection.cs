using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Infrastructure.Persistence;
using RecipeApp.Infrastructure.Persistence.Repositories;
using RecipeApp.Infrastructure.Repositories;
using RecipeApp.Infrastructure.Services;
using RecipeApp.Application.Common.Options;


namespace RecipeApp.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        services.AddHttpClient<ITheMealDbService, TheMealDbService>();

        // Bind Keycloak admin options
        services.Configure<KeycloakAdminOptions>(configuration.GetSection("KeycloakAdmin"));


        var connectionString = configuration.GetConnectionString("DefaultConnection");

        services.AddDbContext<RecipeAppDbContext>(options =>
            options.UseMySql(connectionString, ServerVersion.AutoDetect(connectionString)));

        // Repositories
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IRecipeRepository, RecipeRepository>();
        services.AddScoped<IFavoriteRepository, FavoriteRepository>();
        
        // Auth Service (Keycloak + DB)
        services.AddHttpClient<IAuthService, KeycloakAuthService>()
    .AddTypedClient((httpClient, sp) =>
    {
        var config = sp.GetRequiredService<IConfiguration>();
        var db = sp.GetRequiredService<RecipeAppDbContext>();
        return new KeycloakAuthService(httpClient, config, db);
    });



        // TheMealDB API client
        services.AddHttpClient<ITheMealDbService, TheMealDbService>();


        return services;
    }
}
