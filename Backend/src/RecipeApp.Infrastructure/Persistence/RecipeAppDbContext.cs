using Microsoft.EntityFrameworkCore;
using RecipeApp.Domain.Entities;

namespace RecipeApp.Infrastructure.Persistence;

public class RecipeAppDbContext : DbContext
{
    public RecipeAppDbContext(DbContextOptions<RecipeAppDbContext> options) : base(options) { }

    public DbSet<User> Users => Set<User>();
    public DbSet<Recipe> Recipes => Set<Recipe>();
    public DbSet<RecipeIngredient> RecipeIngredients => Set<RecipeIngredient>();
    
    public DbSet<FavoriteRecipe> FavoriteRecipes => Set<FavoriteRecipe>();

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

    // youtube link
            modelBuilder.Entity<Recipe>(entity =>
{
  entity.Property(r => r.YoutubeUrl)
        .HasColumnType("longtext")
        .IsRequired(false)
        .UsePropertyAccessMode(PropertyAccessMode.Property);
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

            // FavoriteRecipe
            // Favorites
            modelBuilder.Entity<FavoriteRecipe>(entity =>
            {
                  entity.HasIndex(f => new { f.UserId, f.RecipeId }).IsUnique();

                  entity.HasOne(f => f.Recipe)
                        .WithMany()
                        .HasForeignKey(f => f.RecipeId)
                        .OnDelete(DeleteBehavior.Cascade);

           
            // not using FK to Users now (Keycloak is the user source of truth)
            // entity.HasOne(f => f.User)
                  //       .WithMany()
                  //       .HasForeignKey(f => f.UserId)
                  //       .OnDelete(DeleteBehavior.Cascade);
            });


      }
}
