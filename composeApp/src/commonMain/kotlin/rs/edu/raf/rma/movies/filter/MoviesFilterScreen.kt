package rs.edu.raf.rma.movies.filter

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rs.edu.raf.rma.movies.list.MoviesListContract

private val BgDark = Color(0xFF14181C)
private val BgSurface = Color(0xFF22272E)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8899AA)
private val AccentGreen = Color(0xFF00E054)

@Composable
fun MoviesFilterScreen(
    viewModel: MoviesFilterViewModel,
    initialFilters: MoviesListContract.ActiveFilters,
    onApplyFilters: (MoviesListContract.ActiveFilters) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.initializeWith(initialFilters)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MoviesFilterContract.SideEffect.FiltersApplied -> onApplyFilters(effect.filters)
                MoviesFilterContract.SideEffect.NavigateBack -> onBack()
            }
        }
    }

    val state by viewModel.state.collectAsState()

    MoviesFilterScreen(
        state = state,
        onQueryChange = viewModel::setQuery,
        onGenreToggle = { genreId ->
            viewModel.setGenre(if (state.pendingGenreId == genreId) null else genreId)
        },
        onMinYearChange = viewModel::setMinYear,
        onMaxYearChange = viewModel::setMaxYear,
        onMinRatingChange = viewModel::setMinRating,
        onClearAll = { viewModel.setEvent(MoviesFilterContract.UiEvent.ClearAll) },
        onApplyFilters = { viewModel.setEvent(MoviesFilterContract.UiEvent.ApplyFilters) },
        onBack = { viewModel.setEvent(MoviesFilterContract.UiEvent.NavigateBack) },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MoviesFilterScreen(
    state: MoviesFilterContract.UiState,
    onQueryChange: (String) -> Unit,
    onGenreToggle: (Int) -> Unit,
    onMinYearChange: (String) -> Unit,
    onMaxYearChange: (String) -> Unit,
    onMinRatingChange: (Float) -> Unit,
    onClearAll: () -> Unit,
    onApplyFilters: () -> Unit,
    onBack: () -> Unit,
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = AccentGreen,
        unfocusedBorderColor = TextMuted.copy(alpha = 0.4f),
        cursorColor = AccentGreen,
        focusedLabelColor = AccentGreen,
        unfocusedLabelColor = TextMuted,
        focusedPlaceholderColor = TextMuted,
        unfocusedPlaceholderColor = TextMuted.copy(alpha = 0.6f),
        focusedContainerColor = BgSurface,
        unfocusedContainerColor = BgSurface,
    )

    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = {
                    Text(
                        text = "Filter",
                        color = TextPrimary,
                    )
                },
                actions = {
                    TextButton(onClick = onClearAll) {
                        Text(
                            text = "Clear All",
                            color = AccentGreen,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader(title = "SEARCH")
            OutlinedTextField(
                value = state.pendingQuery,
                onValueChange = onQueryChange,
                placeholder = { Text(text = "Movie title...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                colors = textFieldColors,
            )

            SectionDivider()

            SectionHeader(title = "GENRE")
            if (state.isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            } else {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    state.genres.forEach { genre ->
                        val selected = state.pendingGenreId == genre.id
                        FilterChip(
                            selected = selected,
                            onClick = { onGenreToggle(genre.id) },
                            label = {
                                Text(
                                    text = genre.name,
                                    color = if (selected) AccentGreen else TextMuted,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = BgSurface,
                                selectedContainerColor = AccentGreen.copy(alpha = 0.15f),
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = TextMuted.copy(alpha = 0.3f),
                                selectedBorderColor = AccentGreen,
                            ),
                        )
                    }
                }
            }

            SectionDivider()

            SectionHeader(title = "YEAR RANGE")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                OutlinedTextField(
                    value = state.pendingMinYear,
                    onValueChange = onMinYearChange,
                    label = { Text(text = "From") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = textFieldColors,
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.pendingMaxYear,
                    onValueChange = onMaxYearChange,
                    label = { Text(text = "To") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = textFieldColors,
                )
            }

            SectionDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "MINIMUM RATING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (state.pendingMinRating > 0f) {
                        "%.1f+".format(state.pendingMinRating)
                    } else {
                        "Any"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                )
            }
            Slider(
                value = state.pendingMinRating,
                onValueChange = onMinRatingChange,
                valueRange = 0f..10f,
                steps = 19,
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = AccentGreen,
                    activeTrackColor = AccentGreen,
                    inactiveTrackColor = BgSurface,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BgSurface)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onApplyFilters,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = BgDark,
                ),
            ) {
                Text(
                    text = "Apply Filters",
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = BgSurface)
}
