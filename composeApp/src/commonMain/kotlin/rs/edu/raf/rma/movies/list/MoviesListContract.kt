package rs.edu.raf.rma.movies.list

import rs.edu.raf.rma.networking.model.MovieListItem

interface MoviesListContract {

    enum class SortOption(val label: String, val apiValue: String) {
        Rating("Rating", "imdb_rating"),
        Year("Year", "year"),
        Title("Title", "title"),
        Popularity("Popularity", "popularity"),
    }

    data class ActiveFilters(
        val query: String? = null,
        val genreId: Int? = null,
        val minYear: Int? = null,
        val maxYear: Int? = null,
        val minRating: Float? = null,
    ) {
        val count: Int
            get() = listOfNotNull(
                query?.takeIf { it.isNotBlank() },
                genreId,
                minYear,
                maxYear,
                minRating?.takeIf { it > 0f },
            ).size
    }

    data class UiState(
        val movies: List<MovieListItem> = emptyList(),
        val totalCount: Int = 0,
        val isLoading: Boolean = true,
        val error: Throwable? = null,
        val sortOption: SortOption = SortOption.Rating,
        val filters: ActiveFilters = ActiveFilters(),
    ) {
        val activeFilterCount: Int get() = filters.count
    }

    sealed class UiEvent {
        data object OpenFilter : UiEvent()
        data class OpenMovieDetails(val imdbId: String) : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateToFilter : SideEffect()
        data class NavigateToMovieDetails(val imdbId: String) : SideEffect()
    }
}
