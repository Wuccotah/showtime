package rs.edu.raf.rma.movies.filter

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
import rs.edu.raf.rma.movies.list.MoviesListContract
import rs.edu.raf.rma.movies.repository.MoviesRepository

class MoviesFilterViewModel(
    private val repository: MoviesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MoviesFilterContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: MoviesFilterContract.UiState.() -> MoviesFilterContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<MoviesFilterContract.UiEvent>()
    fun setEvent(event: MoviesFilterContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<MoviesFilterContract.SideEffect>()
    val effects = _effects.asSharedFlow()
    private fun setEffect(effect: MoviesFilterContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        loadGenres()
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    MoviesFilterContract.UiEvent.NavigateBack -> {
                        setEffect(MoviesFilterContract.SideEffect.NavigateBack)
                    }
                    MoviesFilterContract.UiEvent.ApplyFilters -> {
                        setEffect(MoviesFilterContract.SideEffect.FiltersApplied(buildFilters()))
                    }
                    MoviesFilterContract.UiEvent.ClearAll -> {
                        clearAll()
                    }
                }
            }
        }
    }

    fun initializeWith(filters: MoviesListContract.ActiveFilters) {
        setState {
            copy(
                pendingQuery = filters.query ?: "",
                pendingGenreId = filters.genreId,
                pendingMinYear = filters.minYear?.toString() ?: "",
                pendingMaxYear = filters.maxYear?.toString() ?: "",
                pendingMinRating = filters.minRating ?: 0f,
            )
        }
    }

    fun setQuery(query: String) {
        setState { copy(pendingQuery = query) }
    }

    fun setGenre(genreId: Int?) {
        setState { copy(pendingGenreId = genreId) }
    }

    fun setMinYear(year: String) {
        setState { copy(pendingMinYear = year) }
    }

    fun setMaxYear(year: String) {
        setState { copy(pendingMaxYear = year) }
    }

    fun setMinRating(rating: Float) {
        setState { copy(pendingMinRating = rating) }
    }

    fun clearAll() {
        setState {
            copy(
                pendingQuery = "",
                pendingGenreId = null,
                pendingMinYear = "",
                pendingMaxYear = "",
                pendingMinRating = 0f,
            )
        }
    }

    fun buildFilters(): MoviesListContract.ActiveFilters {
        val s = _state.value
        return MoviesListContract.ActiveFilters(
            query = s.pendingQuery.takeIf { it.isNotBlank() },
            genreId = s.pendingGenreId,
            minYear = s.pendingMinYear.toIntOrNull(),
            maxYear = s.pendingMaxYear.toIntOrNull(),
            minRating = s.pendingMinRating.takeIf { it > 0f },
        )
    }

    private fun loadGenres() {
        viewModelScope.launch {
            setState { copy(isLoadingGenres = true) }
            val result = withContext(Dispatchers.IO) {
                runCatching { repository.getGenres() }
            }
            result.fold(
                onSuccess = { genres ->
                    setState { copy(isLoadingGenres = false, genres = genres) }
                },
                onFailure = {
                    setState { copy(isLoadingGenres = false) }
                },
            )
        }
    }
}
