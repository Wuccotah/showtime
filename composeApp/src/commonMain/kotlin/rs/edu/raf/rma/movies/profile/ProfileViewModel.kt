package rs.edu.raf.rma.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.core.auth.AuthRepository
import rs.edu.raf.rma.movies.data.MovieRepository
import rs.edu.raf.rma.networking.MoviesApi

class ProfileViewModel(
    private val moviesApi: MoviesApi,
    private val movieRepository: MovieRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private fun setState(reducer: ProfileState.() -> ProfileState) {
        _state.getAndUpdate(reducer)
    }

    private val _effects = MutableSharedFlow<ProfileSideEffect>()
    val effects = _effects.asSharedFlow()

    init {
        loadProfile()
        observeStats()
    }

    fun setEvent(event: ProfileEvent) {
        viewModelScope.launch {
            when (event) {
                ProfileEvent.Logout -> logout()
                ProfileEvent.Retry -> loadProfile()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            runCatching { moviesApi.getMe() }
                .onSuccess { user ->
                    setState { copy(username = user.username, fullName = user.fullName, isLoading = false) }
                }
                .onFailure {
                    setState { copy(error = "Could not load profile", isLoading = false) }
                }
        }
    }

    private fun observeStats() {
        viewModelScope.launch {
            movieRepository.observeFavoritesCount().collect { count ->
                setState { copy(favoritesCount = count) }
            }
        }
        viewModelScope.launch {
            movieRepository.observeWatchlistCount().collect { count ->
                setState { copy(watchlistCount = count) }
            }
        }
        viewModelScope.launch {
            movieRepository.observeBestScore().collect { score ->
                setState { copy(bestScore = score) }
            }
        }
        viewModelScope.launch {
            movieRepository.observeQuizCount().collect { count ->
                setState { copy(quizCount = count) }
            }
        }
    }

    private suspend fun logout() {
        runCatching { authRepository.logout() }
        _effects.emit(ProfileSideEffect.LoggedOut)
    }
}
