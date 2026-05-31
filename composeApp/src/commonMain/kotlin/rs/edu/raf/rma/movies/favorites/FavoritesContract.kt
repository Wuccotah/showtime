package rs.edu.raf.rma.movies.favorites

import rs.edu.raf.rma.demo.MovieItem

interface FavoritesContract {
    data class UiState(
        val movies: List<MovieItem> = emptyList(),
        val isRefreshing: Boolean = false,
        val error: Throwable? = null,
    )

    sealed class UiEvent {
        data class OpenMovieDetails(val imdbId: String) : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToMovieDetails(val imdbId: String) : SideEffect()
    }
}
