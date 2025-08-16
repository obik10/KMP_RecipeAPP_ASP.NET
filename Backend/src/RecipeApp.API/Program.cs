using RecipeApp.Application;
using RecipeApp.Infrastructure;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Infrastructure.Services;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddApplication();
builder.Services.AddInfrastructure(builder.Configuration);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

// ✅ Register Swagger once
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "RecipeApp API",
        Version = "v1",
        Description = "API for managing recipes, ingredients, and images"
    });

    c.OperationFilter<RecipeApp.API.Swagger.FileUploadOperationFilter>();
});

builder.Services.AddHealthChecks();

// File storage using wwwroot
builder.Services.AddScoped<IFileStorage>(sp =>
{
    var env = sp.GetRequiredService<IWebHostEnvironment>();
    var root = env.WebRootPath ?? "wwwroot";
    Directory.CreateDirectory(root); // ensure exists
    Directory.CreateDirectory(Path.Combine(root, "uploads")); // ensure /uploads exists
    return new FileStorage(root);
});

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "RecipeApp API v1");
        c.RoutePrefix = string.Empty; // open swagger at root (http://localhost:5076)
    });
}

app.UseStaticFiles();
app.UseHttpsRedirection();
app.UseAuthorization();

app.MapControllers();
app.MapHealthChecks("/health");

app.Run();
