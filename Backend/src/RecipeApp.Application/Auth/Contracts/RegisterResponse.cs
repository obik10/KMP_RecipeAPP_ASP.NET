namespace RecipeApp.Application.Auth.Contracts;

public class RegisterResponse
{
    public Guid UserId { get; set; }
    public string Username { get; set; } = default!;
    public string Email { get; set; } = default!;
}
