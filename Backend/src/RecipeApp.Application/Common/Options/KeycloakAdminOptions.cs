namespace RecipeApp.Application.Common.Options;

public class KeycloakAdminOptions
{
    public string BaseUrl { get; set; } = default!;
    public string Realm { get; set; } = default!;
    public string ClientId { get; set; } = default!;
    public string ClientSecret { get; set; } = default!;
    public bool RequireHttps { get; set; }
}
