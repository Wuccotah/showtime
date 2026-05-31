package rs.edu.raf.rma.movies.details

import rs.edu.raf.rma.demo.MovieItem
import rs.edu.raf.rma.networking.model.ImageItem
import rs.edu.raf.rma.networking.model.PersonSummary

interface MovieDetailsContract {
    data class UiState(
        val movie: MovieItem? = null,
        val actors: List<PersonSummary> = emptyList(),
        val backdropImages: List<ImageItem> = emptyList(),
        val isRefreshing: Boolean = false,
        val error: Throwable? = null,
    )

    sealed class UiEvent {
        data object NavigateBack : UiEvent()
        data object Refresh : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateBack : SideEffect()
    }
}
