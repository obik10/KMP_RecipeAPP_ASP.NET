using RecipeApp.Application;
using RecipeApp.Infrastructure;
using Microsoft.OpenApi.Models;
using Microsoft.AspNetCore.Mvc;


var builder = WebApplication.CreateBuilder(args);

// Add Application & Infrastructure layers
builder.Services.AddApplication(); // We'll add this extension below
builder.Services.AddInfrastructure(builder.Configuration);

// Add controllers
builder.Services.AddControllers()
    .AddNewtonsoftJson(); // optional, for JSON flexibility

// Add Swagger
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "RecipeApp API",
        Version = "v1"
    });
});

// Health checks
builder.Services.AddHealthChecks();

var app = builder.Build();

// Middleware pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();
app.MapHealthChecks("/health");

app.Run();
