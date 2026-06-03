package rs.edu.raf.rma.movies.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import rs.edu.raf.rma.demo.MovieItem

private val BgDark = Color(0xFF14181C)
private val BgSurface = Color(0xFF22272E)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8899AA)
private val AccentBlue = Color(0xFF40BCF4)
private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onMovieClick: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WatchlistContract.SideEffect.NavigateToMovieDetails ->
                    onMovieClick(effect.imdbId)
            }
        }
    }

    WatchlistScreen(
        state = state,
        onMovieClick = { viewModel.setEvent(WatchlistContract.UiEvent.OpenMovieDetails(it)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchlistScreen(
    state: WatchlistContract.UiState,
    onMovieClick: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Watchlist", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark),
            windowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        )

        HorizontalDivider(color = BgSurface)

        if (state.movies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Your watchlist is empty.\nTap 🔖 on a movie to add it.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.movies, key = { it.imdbId }) { movie ->
                    WatchlistMovieItem(
                        movie = movie,
                        onClick = { onMovieClick(movie.imdbId) },
                    )
                    HorizontalDivider(
                        color = BgSurface,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchlistMovieItem(
    movie: MovieItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = "${IMAGE_BASE_URL}w185${movie.posterPath}",
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 52.dp, height = 78.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(BgSurface),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (movie.year != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = movie.year.toString(), fontSize = 12.sp, color = TextMuted)
            }
            if (movie.imdbRating != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "★", fontSize = 12.sp, color = Color(0xFFF5C518))
                    Text(text = movie.imdbRating.toString(), fontSize = 12.sp, color = TextPrimary)
                }
            }
        }
    }
}
