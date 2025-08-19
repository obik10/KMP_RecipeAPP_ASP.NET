using System.Security.Claims;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RecipeApp.API.Contracts.Recipes;
using RecipeApp.Application.Recipes.Commands.CreateRecipe;
using RecipeApp.Application.Recipes.Commands.DeleteRecipe;
using RecipeApp.Application.Recipes.Commands.UpdateRecipe;
using RecipeApp.Application.Recipes.Commands.UploadRecipeImage;
using RecipeApp.Application.Recipes.Commands.Favorites;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Queries.GetRecipeById;
using RecipeApp.Application.Recipes.Queries.ListRecipes;
using RecipeApp.Application.Recipes.Queries.GetMyRecipes;
using RecipeApp.Application.Recipes.Queries.GetFavoriteRecipes;
using RecipeApp.Application.Common.Models;

namespace RecipeApp.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class RecipesController : ControllerBase
{
    private readonly IMediator _mediator;

    public RecipesController(IMediator mediator)
    {
        _mediator = mediator;
    }

    // Helpers
    private bool TryGetUserId(out Guid userId)
    {
        userId = Guid.Empty;
        var sub = User.FindFirstValue(ClaimTypes.NameIdentifier) ?? User.FindFirstValue("sub");
        return Guid.TryParse(sub, out userId);
    }

    // === Public endpoints ===

    // GET /api/recipes?pageNumber=1&pageSize=10
    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<PaginatedResult<RecipeDto>>> List(
        [FromQuery] int pageNumber = 1,
        [FromQuery] int pageSize = 10,
        CancellationToken ct = default)
    {
        var result = await _mediator.Send(new ListRecipesQuery(pageNumber, pageSize), ct);
        return Ok(result);
    }

    // GET /api/recipes/search?keyword=...
    [HttpGet("search")]
    [AllowAnonymous]
    public async Task<ActionResult<IEnumerable<RecipeDto>>> Search([FromQuery] string keyword, CancellationToken ct)
    {
        if (string.IsNullOrWhiteSpace(keyword))
            return BadRequest("Search term is required.");

        var result = await _mediator.Send(new Application.Recipes.Queries.SearchRecipes.SearchRecipesQuery(keyword), ct);
        return Ok(result);
    }

    // GET /api/recipes/{id}
    [HttpGet("{id:guid}")]
    [AllowAnonymous]
    public async Task<ActionResult<RecipeDto>> GetById(Guid id, CancellationToken ct)
    {
        var item = await _mediator.Send(new GetRecipeByIdQuery(id), ct);
        return item is null ? NotFound() : Ok(item);
    }

    // === Protected endpoints (auth required) ===

    [HttpPost]
    [Authorize(Policy = "CanWriteRecipes")]
    public async Task<ActionResult<RecipeDto>> Create([FromBody] CreateRecipeRequest request, CancellationToken ct)
    {
        Guid? ownerId = null;
        if (TryGetUserId(out var uid))
            ownerId = uid;

        var cmd = new CreateRecipeCommand(
            request.Title,
            request.Instructions,
            ownerId,
            request.Ingredients.Select(i => new Application.Recipes.Commands.Shared.RecipeIngredientInput(i.Name, i.Measure)).ToList(),
            request.YoutubeUrl
        );

        var created = await _mediator.Send(cmd, ct);
        return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
    }

    [HttpPut("{id:guid}")]
    [Authorize(Policy = "CanWriteRecipes")]
    public async Task<ActionResult<RecipeDto>> Update(Guid id, [FromBody] UpdateRecipeRequest request, CancellationToken ct)
    {
        var cmd = new UpdateRecipeCommand(
            id,
            request.Title,
            request.Instructions,
            request.Ingredients.Select(i => new Application.Recipes.Commands.Shared.RecipeIngredientInput(i.Name, i.Measure)).ToList(),
            request.YoutubeUrl
        );

        var updated = await _mediator.Send(cmd, ct);
        return Ok(updated);
    }

    [HttpDelete("{id:guid}")]
    [Authorize(Policy = "CanWriteRecipes")]
    public async Task<IActionResult> Delete(Guid id, CancellationToken ct)
    {
        await _mediator.Send(new DeleteRecipeCommand(id), ct);
        return NoContent();
    }

    [HttpPost("{id:guid}/image-recipe")]
    [Authorize(Policy = "CanWriteRecipes")]
    [RequestSizeLimit(10 * 1024 * 1024)] // 10MB
    public async Task<ActionResult<RecipeDto>> UploadImage(
        Guid id,
        [FromForm] UploadImageRequest request,
        CancellationToken ct)
    {
        if (request.File is null || request.File.Length == 0)
            return BadRequest("Image file is required.");

        var cmd = new UploadRecipeImageCommand(
            id,
            request.File.OpenReadStream(),
            request.File.FileName,
            request.File.ContentType
        );

        var updated = await _mediator.Send(cmd, ct);
        return Ok(updated);
    }

    // === Favorites + MyRecipes ===

    [HttpGet("myrecipes")]
    [Authorize]
    public async Task<IActionResult> GetMyRecipes(CancellationToken ct)
    {
        if (!TryGetUserId(out var userId))
            return Unauthorized();

        var result = await _mediator.Send(new GetMyRecipesQuery(userId), ct);
        return Ok(result);
    }

    [HttpPost("{id:guid}/add-favorite")]
    [Authorize]
    public async Task<IActionResult> AddFavorite(Guid id, CancellationToken ct)
    {
        if (!TryGetUserId(out var userId))
            return Unauthorized();

        await _mediator.Send(new AddFavoriteCommand(userId, id), ct);
        return NoContent();
    }

    [HttpDelete("{id:guid}/delete-favorite")]
    [Authorize]
    public async Task<IActionResult> RemoveFavorite(Guid id, CancellationToken ct)
    {
        if (!TryGetUserId(out var userId))
            return Unauthorized();

        await _mediator.Send(new RemoveFavoriteCommand(userId, id), ct);
        return NoContent();
    }

    [HttpGet("myfavorites")]
    [Authorize]
    public async Task<IActionResult> GetFavorites(CancellationToken ct)
    {
        if (!TryGetUserId(out var userId))
            return Unauthorized();

        var result = await _mediator.Send(new GetFavoriteRecipesQuery(userId), ct);
        return Ok(result);
    }
}
