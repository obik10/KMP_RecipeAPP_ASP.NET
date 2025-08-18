using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using RecipeApp.Domain.Entities;

namespace RecipeApp.Infrastructure.Persistence.Configurations;

public class RecipeConfiguration : IEntityTypeConfiguration<Recipe>
{
    public void Configure(EntityTypeBuilder<Recipe> builder)
    {
        builder.HasKey(r => r.Id);

        builder.Property(r => r.Title)
            .IsRequired()
            .HasMaxLength(200);

        builder.Property(r => r.Instructions)
            .IsRequired();

        builder.Property(r => r.ImagePath)
            .HasMaxLength(500);

        builder.HasMany(r => r.Ingredients)
            .WithOne()
            .HasForeignKey(i => i.RecipeId)
            .OnDelete(DeleteBehavior.Cascade);

        // External recipe properties
        builder.Property(r => r.IsExternal)
            .IsRequired()
            .HasDefaultValue(false);

        //youtbube link
            builder.Property(r => r.YoutubeUrl)
    .HasMaxLength(500);

        builder.Property(r => r.ExternalSource)
            .HasMaxLength(100);

        builder.Property(r => r.ExternalId)
            .HasMaxLength(100);

        builder.HasIndex(r => new { r.ExternalSource, r.ExternalId })
            .IsUnique()
            .HasFilter("`ExternalSource` IS NOT NULL AND `ExternalId` IS NOT NULL");
    }
}
