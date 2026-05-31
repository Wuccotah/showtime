package rs.edu.raf.rma.movies.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import rs.edu.raf.rma.demo.MovieDetails

@Composable
fun MovieDetailsScreen(
    viewModel: MovieDetailsViewModel,
    onBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                MovieDetailsContract.SideEffect.NavigateBack -> onBack()
            }
        }
    }

    MovieDetailsScreen(
        state = state,
        onBack = { viewModel.setEvent(MovieDetailsContract.UiEvent.NavigateBack) },
        onToggleFavorite = { viewModel.setEvent(MovieDetailsContract.UiEvent.ToggleFavorite) },
        onToggleWatchlist = { viewModel.setEvent(MovieDetailsContract.UiEvent.ToggleWatchlist) },
    )
}

@Composable
private fun MovieDetailsScreen(
    state: MovieDetailsContract.UiState,
    onBack: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onToggleWatchlist: () -> Unit = {},
) {
    when {
        state.isRefreshing && state.movie == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Error: ${state.error.message}")
            }
        }
        state.movie != null -> {
            MovieDetails(
                movie = state.movie,
                director = state.movie.directorName,
                actors = state.actors.map { it.name },
                trailerUrl = state.movie.trailerUrl,
                backdropImages = state.backdropImages,
                isFavorite = state.isFavorite,
                isOnWatchlist = state.isOnWatchlist,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite,
                onToggleWatchlist = onToggleWatchlist,
            )
        }
    }
}
