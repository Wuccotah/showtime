package rs.edu.raf.rma.movies.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.data.MovieRepository

class MoviesListViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MoviesListContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: MoviesListContract.UiState.() -> MoviesListContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<MoviesListContract.UiEvent>()
    fun setEvent(event: MoviesListContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    fun alternativeSetEvent(event: MoviesListContract.UiEvent) {
        viewModelScope.launch {
            when (event) {
                is MoviesListContract.UiEvent.Refresh -> refresh()
                is MoviesListContract.UiEvent.OpenMovieDetails ->
                    setEffect(MoviesListContract.SideEffect.NavigateToMovieDetails(event.imdbId))
                MoviesListContract.UiEvent.OpenFilter ->
                    setEffect(MoviesListContract.SideEffect.NavigateToFilter)
            }
        }
    }

    private val _effects = MutableSharedFlow<MoviesListContract.SideEffect>()
    val effects = _effects.asSharedFlow()
    private fun setEffect(effect: MoviesListContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        observeMovies()
        refresh()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is MoviesListContract.UiEvent.Refresh -> refresh()
                    is MoviesListContract.UiEvent.OpenMovieDetails ->
                        setEffect(MoviesListContract.SideEffect.NavigateToMovieDetails(event.imdbId))
                    MoviesListContract.UiEvent.OpenFilter ->
                        setEffect(MoviesListContract.SideEffect.NavigateToFilter)
                }
            }
        }
    }

    private fun observeMovies() {
        viewModelScope.launch {
            repository.observeMovies().collect { movies ->
                setState { copy(movies = movies) }
            }
        }
    }

    private fun refresh() {
        val sortOption = _state.value.sortOption
        val filters = _state.value.filters
        viewModelScope.launch {
            setState { copy(isRefreshing = true, error = null) }
            runCatching {
                repository.refreshMovies(
                    query = filters.query?.takeIf { it.isNotBlank() },
                    genreId = filters.genreId,
                    minYear = filters.minYear,
                    maxYear = filters.maxYear,
                    minRating = filters.minRating,
                    sortBy = sortOption.apiValue,
                    sortOrder = "desc",
                )
            }.onFailure { setState { copy(error = it) } }
            setState { copy(isRefreshing = false) }
        }
    }

    fun setSortOption(option: MoviesListContract.SortOption) {
        setState { copy(sortOption = option) }
        refresh()
    }

    fun applyFilters(filters: MoviesListContract.ActiveFilters) {
        setState { copy(filters = filters) }
        refresh()
    }
}
