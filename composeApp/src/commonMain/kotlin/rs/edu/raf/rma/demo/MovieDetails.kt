package rs.edu.raf.rma.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import rs.edu.raf.rma.networking.model.ImageItem

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

private val bannerHeight = 220.dp
private val posterHeight = 180.dp
private val posterWidth = 120.dp
private val posterOverlap = posterHeight / 2

private val BgDark = Color(0xFF14181C)
private val BgSurface = Color(0xFF22272E)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8899AA)
private val AccentGreen = Color(0xFF00E054)
private val AccentBlue = Color(0xFF40BCF4)

@Composable
fun MovieDetails(
    movie: MovieItem,
    director: String? = null,
    actors: List<String> = emptyList(),
    trailerUrl: String? = null,
    backdropImages: List<ImageItem> = emptyList(),
    isFavorite: Boolean = false,
    isOnWatchlist: Boolean = false,
    onBack: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onToggleWatchlist: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight + posterOverlap),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bannerHeight),
                ) {
                    AsyncImage(
                        model = "${IMAGE_BASE_URL}w780${movie.backdropPath}",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BgSurface),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, BgDark),
                                ),
                            ),
                    )

                    IconButton(
                        onClick = { trailerUrl?.let { uriHandler.openUri(it) } },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Color.Black.copy(alpha = if (trailerUrl != null) 0.55f else 0.3f),
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play Trailer",
                            tint = Color.White.copy(alpha = if (trailerUrl != null) 1f else 0.35f),
                            modifier = Modifier.size(38.dp),
                        )
                    }
                }

                AsyncImage(
                    model = "${IMAGE_BASE_URL}w342${movie.posterPath}",
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp)
                        .width(posterWidth)
                        .height(posterHeight)
                        .offset(y = bannerHeight - posterOverlap)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, end = posterWidth + 32.dp)
                        .height(posterOverlap),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = movie.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${movie.year} · DIRECTED BY",
                        fontSize = 10.sp,
                        color = TextMuted,
                        letterSpacing = 0.8.sp,
                    )
                    Text(
                        text = director ?: "Unknown",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark)
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 24.dp),
            ) {
                if (movie.runtime != null) {
                    Text(
                        text = "${movie.runtime} mins",
                        fontSize = 12.sp,
                        color = TextMuted,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = movie.imdbRating?.toString() ?: "N/A",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                    )
                    if (movie.imdbVotes != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${formatVotes(movie.imdbVotes)} votes)",
                            fontSize = 12.sp,
                            color = TextMuted,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (movie.tmdbRating != null) {
                        Text(
                            text = "TMDB: ${"%.1f".format(movie.tmdbRating)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentBlue,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (movie.genres.isNotEmpty()) {
                    Text(
                        text = movie.genres.joinToString(" · ") { it.name },
                        fontSize = 12.sp,
                        color = AccentBlue,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BgSurface)
                Spacer(modifier = Modifier.height(16.dp))

                if (!movie.overview.isNullOrBlank()) {
                    Text(
                        text = movie.overview,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFFCCDDEE),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = BgSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "INFO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfoBox(label = "BUDGET", value = formatMoney(movie.budget), modifier = Modifier.weight(1f))
                    InfoBox(label = "REVENUE", value = formatMoney(movie.revenue), modifier = Modifier.weight(1f))
                    InfoBox(label = "LANGUAGE", value = movie.languageCode?.uppercase() ?: "N/A", modifier = Modifier.weight(1f))
                    InfoBox(label = "POPULARITY", value = movie.popularity?.let { "%.1f".format(it) } ?: "N/A", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = BgSurface)
                Spacer(modifier = Modifier.height(16.dp))

                if (backdropImages.isNotEmpty()) {
                    Text(
                        text = "IMAGES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        backdropImages.forEach { image ->
                            if (image.filePath != null) {
                                AsyncImage(
                                    model = "${IMAGE_BASE_URL}w780${image.filePath}",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(112.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BgSurface),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = BgSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (actors.isNotEmpty()) {
                    Text(
                        text = "CAST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    actors.forEach { name ->
                        Text(
                            text = name,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 3.dp),
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(
                onClick = onToggleWatchlist,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
            ) {
                Icon(
                    imageVector = if (isOnWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isOnWatchlist) "Remove from watchlist" else "Add to watchlist",
                    tint = if (isOnWatchlist) AccentBlue else Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) AccentGreen else Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun InfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, BgSurface, RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatMoney(amount: Long?): String {
    if (amount == null || amount == 0L) return "N/A"
    return when {
        amount >= 1_000_000_000L -> "$${amount / 1_000_000_000L}B"
        amount >= 1_000_000L -> "$${amount / 1_000_000L}M"
        else -> "$$amount"
    }
}

private fun formatVotes(votes: Int): String {
    return when {
        votes >= 1_000_000 -> "${votes / 1_000_000}M"
        votes >= 1_000 -> "${votes / 1_000}K"
        else -> "$votes"
    }
}
