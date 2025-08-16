using Microsoft.EntityFrameworkCore;
using RecipeApp.Domain.Entities;

namespace RecipeApp.Infrastructure.Persistence;

public class RecipeAppDbContext : DbContext
{
    public RecipeAppDbContext(DbContextOptions<RecipeAppDbContext> options) : base(options) { }

    public DbSet<User> Users => Set<User>();
    public DbSet<Recipe> Recipes => Set<Recipe>();
    public DbSet<RecipeIngredient> RecipeIngredients => Set<RecipeIngredient>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Recipe
        modelBuilder.Entity<Recipe>(entity =>
        {
            entity.Property(r => r.Title)
                  .IsRequired()
                  .HasMaxLength(200);

            entity.Property(r => r.Instructions)
                  .IsRequired();

            entity.Property(r => r.ImagePath)
                  .HasMaxLength(500);

            // One-to-many ingredients
            entity.HasMany(r => r.Ingredients)
                  .WithOne()
                  .HasForeignKey(i => i.RecipeId)
                  .OnDelete(DeleteBehavior.Cascade);
        });

        // Ingredient
        modelBuilder.Entity<RecipeIngredient>(entity =>
        {
            entity.Property(i => i.Name)
                  .IsRequired()
                  .HasMaxLength(100);

            entity.Property(i => i.Measure)
                  .IsRequired()
                  .HasMaxLength(100);
        });
    }
}
