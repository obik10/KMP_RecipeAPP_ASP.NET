// using MediatR;
// using Microsoft.AspNetCore.Mvc;
// using RecipeApp.Application.Recipes.Queries.GetPublicRecipes;

// namespace RecipeApp.API.Controllers;

// [ApiController]
// [Route("api/[controller]")]
// public class PublicRecipesController : ControllerBase
// {
//     private readonly IMediator _mediator;

//     public PublicRecipesController(IMediator mediator)
//     {
//         _mediator = mediator;
//     }

//     /// <summary>
//     /// Fetches public recipes from TheMealDB API.
//     /// </summary>
//     /// <param name="search">Search term (e.g., "chicken").</param>
//     [HttpGet]
//     public async Task<IActionResult> Get([FromQuery] string search)
//     {
//         if (string.IsNullOrWhiteSpace(search))
//             return BadRequest("Search term is required.");

//         var recipes = await _mediator.Send(new GetPublicRecipesQuery(search));
//         return Ok(recipes);
//     }
// }
