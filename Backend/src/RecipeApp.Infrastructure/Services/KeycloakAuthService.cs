using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Microsoft.Extensions.Configuration;
using RecipeApp.Application.Auth.Contracts;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Domain.Entities;
using RecipeApp.Infrastructure.Persistence;

namespace RecipeApp.Infrastructure.Services;

public class KeycloakAuthService : IAuthService
{
    private readonly HttpClient _httpClient;
    private readonly IConfiguration _config;
    private readonly RecipeAppDbContext _db;
    private readonly string _realm;
    private readonly string _clientId;
    private readonly string _clientSecret;

    public KeycloakAuthService(HttpClient httpClient, IConfiguration config, RecipeAppDbContext db)
    {
        _httpClient = httpClient;
        _config = config;
        _db = db;

        var adminUrl = _config["KeycloakAdmin:BaseUrl"]
               ?? throw new InvalidOperationException("Missing KeycloakAdmin:BaseUrl in configuration.");

if (!adminUrl.EndsWith("/"))
    adminUrl += "/";

_httpClient.BaseAddress = new Uri(adminUrl);

_realm = _config["KeycloakAdmin:Realm"] 
         ?? throw new InvalidOperationException("Missing KeycloakAdmin:Realm in configuration.");
_clientId = _config["KeycloakAdmin:ClientId"] 
         ?? throw new InvalidOperationException("Missing KeycloakAdmin:ClientId in configuration.");
_clientSecret = _config["KeycloakAdmin:ClientSecret"] 
         ?? throw new InvalidOperationException("Missing KeycloakAdmin:ClientSecret in configuration.");

    }

    public async Task<RegisterResponse> RegisterAsync(RegisterRequest request, CancellationToken cancellationToken = default)
    {
        // 1️⃣ Get admin access token
        var tokenResponse = await _httpClient.PostAsync(
            $"realms/{_realm}/protocol/openid-connect/token",
            new FormUrlEncodedContent(new Dictionary<string, string>
            {
                { "client_id", _clientId },
                { "client_secret", _clientSecret },
                { "grant_type", "client_credentials" }
            }), cancellationToken);

        tokenResponse.EnsureSuccessStatusCode();

        var tokenJson = await tokenResponse.Content.ReadAsStringAsync(cancellationToken);
        using var tokenDoc = JsonDocument.Parse(tokenJson);

        if (!tokenDoc.RootElement.TryGetProperty("access_token", out var accessTokenElement))
            throw new ApplicationException("Keycloak Admin did not return an access_token.");

        var accessToken = accessTokenElement.GetString()
                           ?? throw new ApplicationException("Access token was null.");

        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", accessToken);

     // 2️⃣ Create user in Keycloak
        var userPayload = new
    {
        username = request.Username,
        email = request.Email,
        enabled = true,
        emailVerified = true,
        firstName = request.Username,
        lastName = "User",
        credentials = new[]
        {
            new { type = "password", value = request.Password, temporary = false }
        },
        requiredActions = Array.Empty<string>()
    };



        var content = new StringContent(JsonSerializer.Serialize(userPayload), Encoding.UTF8, "application/json");

        var createResponse = await _httpClient.PostAsync(
            $"admin/realms/{_realm}/users",
            content, cancellationToken);

        if (!createResponse.IsSuccessStatusCode)
        {
            var err = await createResponse.Content.ReadAsStringAsync(cancellationToken);
            throw new ApplicationException($"Keycloak registration failed: {err}");
        }

        // 3️⃣ Extract Keycloak userId from Location header
        var location = createResponse.Headers.Location?.ToString();
        var keycloakId = location?.Split('/').LastOrDefault();
        if (string.IsNullOrEmpty(keycloakId))
            throw new ApplicationException("Failed to retrieve Keycloak userId from Keycloak response.");

        // 4️⃣ Save user into local DB
        var user = new User(request.Username, request.Email);
        try
        {
            _db.Users.Add(user);
            await _db.SaveChangesAsync(cancellationToken);
        }
        catch (Exception ex)
        {
            // rollback: delete Keycloak user if DB insert fails
            await _httpClient.DeleteAsync($"admin/realms/{_realm}/users/{keycloakId}", cancellationToken);
            throw new ApplicationException($"DB sync failed. Keycloak user rolled back. Error: {ex.Message}");
        }

        // 5️⃣ Return response
        return new RegisterResponse
        {
            UserId = user.Id,
            Username = user.Username,
            Email = user.Email
        };
    }
}
