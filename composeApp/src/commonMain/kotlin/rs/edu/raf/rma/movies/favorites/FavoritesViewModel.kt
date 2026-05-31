package rs.edu.raf.rma.movies.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.data.MovieRepository

class FavoritesViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: FavoritesContract.UiState.() -> FavoritesContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<FavoritesContract.UiEvent>()
    fun setEvent(event: FavoritesContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    fun alternativeSetEvent(event: FavoritesContract.UiEvent) {
        viewModelScope.launch {
            when (event) {
                is FavoritesContract.UiEvent.OpenMovieDetails ->
                    setEffect(FavoritesContract.SideEffect.NavigateToMovieDetails(event.imdbId))
            }
        }
    }

    private val _effects = MutableSharedFlow<FavoritesContract.SideEffect>()
    val effects = _effects.asSharedFlow()
    private fun setEffect(effect: FavoritesContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        observeFavorites()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is FavoritesContract.UiEvent.OpenMovieDetails ->
                        setEffect(FavoritesContract.SideEffect.NavigateToMovieDetails(event.imdbId))
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavorites().collect { movies ->
                setState { copy(movies = movies) }
            }
        }
    }
}
