package rs.edu.raf.rma.movies.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import rs.edu.raf.rma.movies.movieIdOrThrow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.data.MovieRepository
import rs.edu.raf.rma.movies.movieIdOrThrow
import rs.edu.raf.rma.networking.model.ImageItem

class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
) : ViewModel() {

    private val movieId = savedStateHandle.movieIdOrThrow

    private val _state = MutableStateFlow(MovieDetailsContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: MovieDetailsContract.UiState.() -> MovieDetailsContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<MovieDetailsContract.UiEvent>()
    fun setEvent(event: MovieDetailsContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    fun alternativeSetEvent(event: MovieDetailsContract.UiEvent) {
        viewModelScope.launch {
            when (event) {
                MovieDetailsContract.UiEvent.NavigateBack ->
                    setEffect(MovieDetailsContract.SideEffect.NavigateBack)
                MovieDetailsContract.UiEvent.Refresh -> refresh()
                MovieDetailsContract.UiEvent.ToggleFavorite -> toggleFavorite()
                MovieDetailsContract.UiEvent.ToggleWatchlist -> toggleWatchlist()
            }
        }
    }

    private val _effects = MutableSharedFlow<MovieDetailsContract.SideEffect>()
    val effects = _effects.asSharedFlow()
    private fun setEffect(effect: MovieDetailsContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        observeMovie()
        observeActors()
        observeBackdrops()
        observeIsFavorite()
        observeIsOnWatchlist()
        refresh()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    MovieDetailsContract.UiEvent.NavigateBack ->
                        setEffect(MovieDetailsContract.SideEffect.NavigateBack)
                    MovieDetailsContract.UiEvent.Refresh -> refresh()
                    MovieDetailsContract.UiEvent.ToggleFavorite -> toggleFavorite()
                    MovieDetailsContract.UiEvent.ToggleWatchlist -> toggleWatchlist()
                }
            }
        }
    }

    private fun observeMovie() {
        viewModelScope.launch {
            repository.observeMovie(movieId).collect { movie ->
                setState { copy(movie = movie) }
            }
        }
    }

    private fun observeActors() {
        viewModelScope.launch {
            repository.observeMovieActors(movieId).collect { actors ->
                setState { copy(actors = actors) }
            }
        }
    }

    private fun observeBackdrops() {
        viewModelScope.launch {
            repository.observeMovieBackdrops(movieId).collect { paths ->
                setState { copy(backdropImages = paths.map { ImageItem(filePath = it) }) }
            }
        }
    }

    private fun observeIsFavorite() {
        viewModelScope.launch {
            repository.isFavorite(movieId).collect { isFav ->
                setState { copy(isFavorite = isFav) }
            }
        }
    }

    private fun observeIsOnWatchlist() {
        viewModelScope.launch {
            repository.isOnWatchlist(movieId).collect { isWatchlisted ->
                setState { copy(isOnWatchlist = isWatchlisted) }
            }
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            val add = !_state.value.isFavorite
            runCatching { repository.toggleFavorite(movieId, add) }
        }
    }

    private fun toggleWatchlist() {
        viewModelScope.launch {
            val add = !_state.value.isOnWatchlist
            runCatching { repository.toggleWatchlist(movieId, add) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isRefreshing = true, error = null) }
            runCatching { repository.refreshMovieDetail(movieId) }
                .onFailure { setState { copy(error = it) } }
            setState { copy(isRefreshing = false) }
        }
    }
}
