package rs.edu.raf.rma.movies.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rs.edu.raf.rma.movies.repository.MoviesRepository

class MoviesListViewModel(
    private val repository: MoviesRepository,
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

    private val _effects = MutableSharedFlow<MoviesListContract.SideEffect>()
    val effects = _effects.asSharedFlow()
    private fun setEffect(effect: MoviesListContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        loadMovies()
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is MoviesListContract.UiEvent.OpenMovieDetails -> {
                        setEffect(MoviesListContract.SideEffect.NavigateToMovieDetails(event.imdbId))
                    }
                    MoviesListContract.UiEvent.OpenFilter -> {
                        setEffect(MoviesListContract.SideEffect.NavigateToFilter)
                    }
                }
            }
        }
    }

    fun setSortOption(option: MoviesListContract.SortOption) {
        setState { copy(sortOption = option) }
        loadMovies()
    }

    fun applyFilters(filters: MoviesListContract.ActiveFilters) {
        setState { copy(filters = filters) }
        loadMovies()
    }

    fun loadMovies() {
        val sortOption = _state.value.sortOption
        val filters = _state.value.filters
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    repository.getMovies(
                        query = filters.query?.takeIf { it.isNotBlank() },
                        pageSize = 30,
                        genreId = filters.genreId,
                        minYear = filters.minYear,
                        maxYear = filters.maxYear,
                        minRating = filters.minRating,
                        sortBy = sortOption.apiValue,
                        sortOrder = "desc",
                    )
                }
            }
            result.fold(
                onSuccess = { response ->
                    setState { copy(isLoading = false, movies = response.items, totalCount = response.totalItems) }
                },
                onFailure = { error ->
                    setState { copy(isLoading = false, error = error) }
                }
            )
        }
    }
}
