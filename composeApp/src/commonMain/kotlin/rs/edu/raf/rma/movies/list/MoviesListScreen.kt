package rs.edu.raf.rma.movies.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

private val BgDark = Color(0xFF14181C)
private val BgSurface = Color(0xFF22272E)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8899AA)
private val AccentGreen = Color(0xFF00E054)
private val AccentBlue = Color(0xFF40BCF4)

@Composable
fun MoviesListScreen(
    viewModel: MoviesListViewModel,
    onMovieClick: (imdbId: String) -> Unit,
    onFilterClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MoviesListContract.SideEffect.NavigateToMovieDetails -> onMovieClick(effect.imdbId)
                MoviesListContract.SideEffect.NavigateToFilter -> onFilterClick()
            }
        }
    }

    MoviesListScreen(
        state = state,
        onMovieClick = { viewModel.setEvent(MoviesListContract.UiEvent.OpenMovieDetails(it)) },
        onFilterClick = { viewModel.setEvent(MoviesListContract.UiEvent.OpenFilter) },
        onSortOptionSelected = viewModel::setSortOption,
        onRetry = { viewModel.setEvent(MoviesListContract.UiEvent.Refresh) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoviesListScreen(
    state: MoviesListContract.UiState,
    onMovieClick: ((imdbId: String) -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onSortOptionSelected: ((MoviesListContract.SortOption) -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
) {
    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary,
                ),
                title = {
                    Text(
                        text = "Showtime",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                },
                actions = {
                    IconButton(onClick = { onFilterClick?.invoke() }) {
                        BadgedBox(
                            badge = {
                                if (state.activeFilterCount > 0) {
                                    Badge(
                                        containerColor = AccentGreen,
                                        contentColor = BgDark,
                                    ) {
                                        Text(text = state.activeFilterCount.toString())
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = TextPrimary,
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SortBar(
                currentSort = state.sortOption,
                totalCount = state.totalCount,
                onSortOptionSelected = onSortOptionSelected,
            )
            HorizontalDivider(color = BgSurface)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    state.isRefreshing && state.movies.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AccentGreen,
                        )
                    }

                    state.error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Something went wrong.",
                                color = TextPrimary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onRetry?.invoke() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentGreen,
                                    contentColor = BgDark,
                                ),
                            ) {
                                Text(text = "Retry")
                            }
                        }
                    }

                    state.movies.isEmpty() -> {
                        Text(
                            text = "No movies found.",
                            color = TextMuted,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            state.movies.forEach { movie ->
                                MovieListItem(
                                    movie = movie,
                                    onClick = { onMovieClick?.invoke(movie.imdbId) },
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
        }
    }
}

@Composable
private fun SortBar(
    currentSort: MoviesListContract.SortOption,
    totalCount: Int,
    onSortOptionSelected: ((MoviesListContract.SortOption) -> Unit)?,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Row(
                modifier = Modifier.clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Sort: ",
                    fontSize = 14.sp,
                    color = TextMuted,
                )
                Text(
                    text = currentSort.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentGreen,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(BgSurface),
            ) {
                MoviesListContract.SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                color = if (option == currentSort) AccentGreen else TextPrimary,
                                fontWeight = if (option == currentSort) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        onClick = {
                            onSortOptionSelected?.invoke(option)
                            expanded = false
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (totalCount > 0) {
            Text(
                text = "$totalCount movies",
                fontSize = 13.sp,
                color = TextMuted,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MovieListItem(
    movie: MovieItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncImage(
            model = "${IMAGE_BASE_URL}w185${movie.posterPath}",
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 72.dp, height = 108.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(BgSurface),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (movie.year != null) {
                Text(
                    text = movie.year.toString(),
                    fontSize = 12.sp,
                    color = TextMuted,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFF5C518),
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = movie.imdbRating.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                if (movie.imdbVotes != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatVotes(movie.imdbVotes),
                        fontSize = 11.sp,
                        color = TextMuted,
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy((-4).dp),
            ) {
                movie.genres.forEach { genre ->
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = AccentBlue.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(50),
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = genre.name,
                            fontSize = 11.sp,
                            color = AccentBlue.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }
    }
}

private fun formatVotes(votes: Int): String {
    return when {
        votes >= 1_000_000 -> "${votes / 1_000_000}M votes"
        votes >= 1_000 -> "${votes / 1_000}K votes"
        else -> "$votes votes"
    }
}
