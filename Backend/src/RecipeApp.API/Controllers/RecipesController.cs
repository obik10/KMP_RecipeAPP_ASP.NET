using MediatR;
using Microsoft.AspNetCore.Mvc;
using RecipeApp.API.Contracts.Recipes;
using RecipeApp.Application.Recipes.Commands.CreateRecipe;
using RecipeApp.Application.Recipes.Commands.DeleteRecipe;
using RecipeApp.Application.Recipes.Commands.UpdateRecipe;
using RecipeApp.Application.Recipes.Commands.UploadRecipeImage;
using RecipeApp.Application.Recipes.Dtos;
using RecipeApp.Application.Recipes.Queries.GetRecipeById;
using RecipeApp.Application.Recipes.Queries.ListRecipes;

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

    // GET /api/recipes
    [HttpGet]
    public async Task<ActionResult<IEnumerable<RecipeDto>>> List(CancellationToken ct)
    {
        var result = await _mediator.Send(new ListRecipesQuery(), ct);
        return Ok(result);
    }

    [HttpGet("search")]
public async Task<ActionResult<IEnumerable<RecipeDto>>> Search([FromQuery] string keyword, CancellationToken ct)
{
    if (string.IsNullOrWhiteSpace(keyword))
        return BadRequest("Search term is required.");

    var result = await _mediator.Send(new Application.Recipes.Queries.SearchRecipes.SearchRecipesQuery(keyword), ct);
    return Ok(result);
}

    // GET /api/recipes/{id}
    [HttpGet("{id:guid}")]
    public async Task<ActionResult<RecipeDto>> GetById(Guid id, CancellationToken ct)
    {
        var item = await _mediator.Send(new GetRecipeByIdQuery(id), ct);
        return item is null ? NotFound() : Ok(item);
    }

    // POST /api/recipes
    [HttpPost]
    public async Task<ActionResult<RecipeDto>> Create([FromBody] CreateRecipeRequest request, CancellationToken ct)
    {
        var cmd = new CreateRecipeCommand(
            request.Title,
            request.Instructions,
            request.OwnerId,
            request.Ingredients.Select(i => new Application.Recipes.Commands.Shared.RecipeIngredientInput(i.Name, i.Measure)).ToList()
        );

        var created = await _mediator.Send(cmd, ct);

        // Return 201 with location header
        return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
    }

    // PUT /api/recipes/{id}
    [HttpPut("{id:guid}")]
    public async Task<ActionResult<RecipeDto>> Update(Guid id, [FromBody] UpdateRecipeRequest request, CancellationToken ct)
    {
        var cmd = new UpdateRecipeCommand(
            id,
            request.Title,
            request.Instructions,
            request.Ingredients.Select(i => new Application.Recipes.Commands.Shared.RecipeIngredientInput(i.Name, i.Measure)).ToList()
        );

        var updated = await _mediator.Send(cmd, ct);
        return Ok(updated);
    }

    // DELETE /api/recipes/{id}
    [HttpDelete("{id:guid}")]
    public async Task<IActionResult> Delete(Guid id, CancellationToken ct)
    {
        await _mediator.Send(new DeleteRecipeCommand(id), ct);
        return NoContent();
    }

   // POST /api/recipes/{id}/image
[HttpPost("{id:guid}/image")]
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

}
