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
import kotlinx.coroutines.withContext
import rs.edu.raf.rma.movies.repository.MoviesRepository

class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MoviesRepository,
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

    private val _effects = MutableSharedFlow<MovieDetailsContract.SideEffect>()
    val effects = _effects.asSharedFlow()
    private fun setEffect(effect: MovieDetailsContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        loadData()
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    MovieDetailsContract.UiEvent.NavigateBack -> {
                        setEffect(MovieDetailsContract.SideEffect.NavigateBack)
                    }
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val movieDeferred = withContext(Dispatchers.IO) {
                async { runCatching { repository.getMovie(movieId) } }
            }
            val castDeferred = withContext(Dispatchers.IO) {
                async { runCatching { repository.getCast(movieId) } }
            }
            val videosDeferred = withContext(Dispatchers.IO) {
                async { runCatching { repository.getVideos(movieId) } }
            }
            val imagesDeferred = withContext(Dispatchers.IO) {
                async { runCatching { repository.getImages(movieId) } }
            }

            val movie = movieDeferred.await()
            val cast = castDeferred.await()
            val videos = videosDeferred.await()
            val images = imagesDeferred.await()

            movie.fold(
                onSuccess = { m ->
                    val castItems = cast.getOrNull()?.items ?: emptyList()
                    val director = castItems.firstOrNull { it.department == "Directing" }
                    val actors = castItems.filter { it.department == "Acting" }.take(10)
                    val trailerUrl = videos.getOrNull()
                        ?.firstOrNull { it.key != null }
                        ?.key
                        ?.let { "https://www.youtube.com/watch?v=$it" }
                    val backdropImages = images.getOrNull()?.take(5) ?: emptyList()
                    setState {
                        copy(
                            isLoading = false,
                            movie = m,
                            director = director,
                            actors = actors,
                            trailerUrl = trailerUrl,
                            backdropImages = backdropImages,
                        )
                    }
                },
                onFailure = { error ->
                    setState { copy(isLoading = false, error = error) }
                }
            )
        }
    }
}
