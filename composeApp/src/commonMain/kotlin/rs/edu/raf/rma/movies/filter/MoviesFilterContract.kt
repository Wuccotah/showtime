package rs.edu.raf.rma.movies.filter

import rs.edu.raf.rma.demo.Genre
import rs.edu.raf.rma.movies.list.MoviesListContract

interface MoviesFilterContract {

    data class UiState(
        val genres: List<Genre> = emptyList(),
        val isRefreshing: Boolean = false,
        val pendingQuery: String = "",
        val pendingGenreId: Int? = null,
        val pendingMinYear: String = "",
        val pendingMaxYear: String = "",
        val pendingMinRating: Float = 0f,
    )

    sealed class UiEvent {
        data object NavigateBack : UiEvent()
        data object ApplyFilters : UiEvent()
        data object ClearAll : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateBack : SideEffect()
        data class FiltersApplied(val filters: MoviesListContract.ActiveFilters) : SideEffect()
    }
}
