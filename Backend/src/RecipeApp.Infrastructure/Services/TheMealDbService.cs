using System.Net.Http.Json;
using Microsoft.Extensions.Configuration;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Application.Recipes.Dtos;

namespace RecipeApp.Infrastructure.Services;

public class TheMealDbService : ITheMealDbService
{
    private readonly HttpClient _httpClient;

    public TheMealDbService(HttpClient httpClient, IConfiguration config)
    {
        var baseUrl = config["TheMealDb:BaseUrl"]
                      ?? throw new InvalidOperationException("TheMealDb BaseUrl not configured.");
        httpClient.BaseAddress = new Uri(baseUrl);
        _httpClient = httpClient;
    }

    public async Task<IEnumerable<MealDto>> SearchMealsAsync(string search, CancellationToken cancellationToken = default)
    {
        var response = await _httpClient.GetFromJsonAsync<MealDbResponse>($"search.php?s={search}", cancellationToken);
        if (response?.Meals == null) return Enumerable.Empty<MealDto>();

        return response.Meals.Select(m => new MealDto(
            m.IdMeal ?? "",
            m.StrMeal ?? "",
            m.StrCategory ?? "",
            m.StrInstructions ?? "",
            m.StrMealThumb ?? "",
            m.StrYoutube ?? "",
            ExtractIngredients(m)
        ));
    }

    private static List<MealIngredient> ExtractIngredients(MealDbItem m)
    {
        var ingredients = new List<MealIngredient>();

        // TheMealDB has up to 20 ingredient/measure fields
        for (int i = 1; i <= 20; i++)
        {
            var ingredientProp = m.GetType().GetProperty($"StrIngredient{i}")?.GetValue(m)?.ToString();
            var measureProp = m.GetType().GetProperty($"StrMeasure{i}")?.GetValue(m)?.ToString();

            if (!string.IsNullOrWhiteSpace(ingredientProp))
            {
                ingredients.Add(new MealIngredient(
                    ingredientProp.Trim(),
                    string.IsNullOrWhiteSpace(measureProp) ? "" : measureProp.Trim()
                ));
            }
        }

        return ingredients;
    }

    private class MealDbResponse
    {
        public List<MealDbItem>? Meals { get; set; }
    }

    private class MealDbItem
    {
        public string? IdMeal { get; set; }
        public string? StrMeal { get; set; }
        public string? StrCategory { get; set; }
        public string? StrInstructions { get; set; }
        public string? StrMealThumb { get; set; }
        public string? StrYoutube { get; set; }

        // Ingredient & measure fields
        public string? StrIngredient1 { get; set; }
        public string? StrIngredient2 { get; set; }
        public string? StrIngredient3 { get; set; }
        public string? StrIngredient4 { get; set; }
        public string? StrIngredient5 { get; set; }
        public string? StrIngredient6 { get; set; }
        public string? StrIngredient7 { get; set; }
        public string? StrIngredient8 { get; set; }
        public string? StrIngredient9 { get; set; }
        public string? StrIngredient10 { get; set; }
        public string? StrIngredient11 { get; set; }
        public string? StrIngredient12 { get; set; }
        public string? StrIngredient13 { get; set; }
        public string? StrIngredient14 { get; set; }
        public string? StrIngredient15 { get; set; }
        public string? StrIngredient16 { get; set; }
        public string? StrIngredient17 { get; set; }
        public string? StrIngredient18 { get; set; }
        public string? StrIngredient19 { get; set; }
        public string? StrIngredient20 { get; set; }

        public string? StrMeasure1 { get; set; }
        public string? StrMeasure2 { get; set; }
        public string? StrMeasure3 { get; set; }
        public string? StrMeasure4 { get; set; }
        public string? StrMeasure5 { get; set; }
        public string? StrMeasure6 { get; set; }
        public string? StrMeasure7 { get; set; }
        public string? StrMeasure8 { get; set; }
        public string? StrMeasure9 { get; set; }
        public string? StrMeasure10 { get; set; }
        public string? StrMeasure11 { get; set; }
        public string? StrMeasure12 { get; set; }
        public string? StrMeasure13 { get; set; }
        public string? StrMeasure14 { get; set; }
        public string? StrMeasure15 { get; set; }
        public string? StrMeasure16 { get; set; }
        public string? StrMeasure17 { get; set; }
        public string? StrMeasure18 { get; set; }
        public string? StrMeasure19 { get; set; }
        public string? StrMeasure20 { get; set; }
    }
}
