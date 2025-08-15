using Microsoft.AspNetCore.Mvc;
using RecipeApp.Application.Common.Interfaces;
using RecipeApp.Domain.Entities;

namespace RecipeApp.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class TestController : ControllerBase
{
    private readonly IUserRepository _userRepo;

    public TestController(IUserRepository userRepo)
    {
        _userRepo = userRepo;
    }

    [HttpPost("user")]
    public async Task<IActionResult> CreateUser(string username, string email)
    {
        var user = new User(username, email);
        await _userRepo.AddAsync(user);
        return Ok(user.Id);
    }

    [HttpGet("user/{id:guid}")]
    public async Task<IActionResult> GetUser(Guid id)
    {
        var user = await _userRepo.GetByIdAsync(id);
        return user is null ? NotFound() : Ok(user);
    }
}
