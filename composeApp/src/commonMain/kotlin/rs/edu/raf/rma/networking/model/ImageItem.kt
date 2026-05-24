package rs.edu.raf.rma.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageItem(
    val filePath: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val voteAverage: Float? = null,
    val language: String? = null,
)

@Serializable
data class MovieImagesResponse(
    val posters: List<ImageItem> = emptyList(),
    val backdrops: List<ImageItem> = emptyList(),
    val logos: List<ImageItem> = emptyList(),
)
