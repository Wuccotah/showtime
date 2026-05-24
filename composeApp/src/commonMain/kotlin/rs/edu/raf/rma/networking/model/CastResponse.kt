package rs.edu.raf.rma.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonSummary(
    val imdbId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    val profilePath: String? = null,
)

@Serializable
data class CastResponse(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<PersonSummary>,
)
