using Microsoft.EntityFrameworkCore;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Domain.Entities;
using RecipeApp.Infrastructure.Persistence;

namespace RecipeApp.Infrastructure.Repositories;

public class UserRepository : IUserRepository
{
    private readonly RecipeAppDbContext _db;

    public UserRepository(RecipeAppDbContext db) => _db = db;

    public async Task<User?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default) =>
        await _db.Users.FindAsync(new object[] { id }, cancellationToken);

    public async Task AddAsync(User user, CancellationToken cancellationToken = default)
    {
        await _db.Users.AddAsync(user, cancellationToken);
        await _db.SaveChangesAsync(cancellationToken);
    }
}
