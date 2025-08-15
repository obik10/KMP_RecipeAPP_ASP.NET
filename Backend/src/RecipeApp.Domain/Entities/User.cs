using RecipeApp.Domain.Common;

namespace RecipeApp.Domain.Entities;

public class User : Entity
{
    public string Username { get; private set; }
    public string Email { get; private set; }

    // EF Core / serialization
    private User() { }

    public User(string username, string email)
    {
        Username = username;
        Email = email;
    }
}
