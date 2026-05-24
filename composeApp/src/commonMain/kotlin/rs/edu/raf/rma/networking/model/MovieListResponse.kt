package rs.edu.raf.rma.networking.model

import kotlinx.serialization.Serializable
import rs.edu.raf.rma.demo.Genre

@Serializable
data class MovieListItem(
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val posterPath: String? = null,
    val genres: List<Genre> = emptyList(),
)

@Serializable
data class MovieListResponse(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<MovieListItem>,
)
