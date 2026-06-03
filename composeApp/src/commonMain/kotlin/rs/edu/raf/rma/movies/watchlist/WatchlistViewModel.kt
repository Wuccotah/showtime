package rs.edu.raf.rma.movies.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.data.MovieRepository

class WatchlistViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: WatchlistContract.UiState.() -> WatchlistContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<WatchlistContract.UiEvent>()
    fun setEvent(event: WatchlistContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    fun alternativeSetEvent(event: WatchlistContract.UiEvent) {
        viewModelScope.launch {
            when (event) {
                is WatchlistContract.UiEvent.OpenMovieDetails ->
                    setEffect(WatchlistContract.SideEffect.NavigateToMovieDetails(event.imdbId))
            }
        }
    }

    private val _effects = MutableSharedFlow<WatchlistContract.SideEffect>()
    val effects = _effects.asSharedFlow()
    private fun setEffect(effect: WatchlistContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        observeWatchlist()
        sync()
    }

    private fun sync() {
        viewModelScope.launch { repository.syncWatchlist() }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is WatchlistContract.UiEvent.OpenMovieDetails ->
                        setEffect(WatchlistContract.SideEffect.NavigateToMovieDetails(event.imdbId))
                }
            }
        }
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            repository.observeWatchlist().collect { movies ->
                setState { copy(movies = movies) }
            }
        }
    }
}
