import org.robiul.kmprecipeapp.domain.models.Recipe

data class RecipeDetailState(
    val isLoading: Boolean = false,
    val recipe: Recipe? = null,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false
)
