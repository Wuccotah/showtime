package rs.edu.raf.rma.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoItem(
    val key: String? = null,
    val site: String? = null,
    val name: String? = null,
    val type: String? = null,
    val official: Boolean = false,
    val publishedAt: String? = null,
)
