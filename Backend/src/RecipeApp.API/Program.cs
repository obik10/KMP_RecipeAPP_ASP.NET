using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using RecipeApp.Application;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Infrastructure;
using RecipeApp.Infrastructure.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddApplication();
builder.Services.AddInfrastructure(builder.Configuration);

// HttpContext accessor (for current user service)
builder.Services.AddHttpContextAccessor();
builder.Services.AddScoped<ICurrentUserService, CurrentUserService>();


// File storage using wwwroot
builder.Services.AddScoped<IFileStorage>(sp =>
{
    var env = sp.GetRequiredService<IWebHostEnvironment>();
    var root = env.WebRootPath ?? "wwwroot";
    Directory.CreateDirectory(root);
    Directory.CreateDirectory(Path.Combine(root, "uploads"));
    return new FileStorage(root);
});

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

// === Authentication (Keycloak / JWT) ===
// 🔄 Read from "Jwt" section (matches appsettings.json)
var authSection = builder.Configuration.GetSection("Jwt");
var authority = authSection.GetValue<string>("Authority")!;
var audience = authSection.GetValue<string>("Audience")!;
var requireHttpsMetadata = authSection.GetValue<bool>("RequireHttpsMetadata");

builder.Services
    .AddAuthentication(options =>
    {
        options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
        options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
    })
    .AddJwtBearer(options =>
    {
        options.Authority = authority;        // e.g. http://localhost:8080/realms/recipe-app
        options.Audience = audience;          // e.g. recipe-app-api
        options.RequireHttpsMetadata = requireHttpsMetadata;

        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidAudience = audience,
            ValidateAudience = true,
            ValidateIssuerSigningKey = true,
            ValidateLifetime = true
        };
    });

builder.Services.AddAuthorization(options =>
{
    options.AddPolicy("CanWriteRecipes", policy =>
        policy.RequireAuthenticatedUser());
    options.AddPolicy("AdminOnly", policy =>
        policy.RequireRole("admin"));
});

// === Swagger (with JWT auth) ===
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "RecipeApp API",
        Version = "v1",
        Description = "API for managing recipes, ingredients, and images"
    });

    c.OperationFilter<RecipeApp.API.Swagger.FileUploadOperationFilter>();

    var securityScheme = new OpenApiSecurityScheme
    {
        Name = "Authorization",
        Description = "Enter: Bearer {your JWT}",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        Reference = new OpenApiReference
        {
            Type = ReferenceType.SecurityScheme,
            Id = "Bearer"
        }
    };
    c.AddSecurityDefinition("Bearer", securityScheme);
    c.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        { securityScheme, Array.Empty<string>() }
    });
});

builder.Services.AddHealthChecks();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "RecipeApp API v1");
        c.RoutePrefix = string.Empty; // open swagger at root
    });
}

app.UseStaticFiles();
app.UseHttpsRedirection();

app.UseAuthentication(); // must be before UseAuthorization
app.UseAuthorization();

app.MapControllers();
app.MapHealthChecks("/health");

app.Run();
