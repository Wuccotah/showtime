package rs.edu.raf.rma.movies.repository

import rs.edu.raf.rma.demo.Genre
import rs.edu.raf.rma.demo.MovieItem
import rs.edu.raf.rma.networking.MoviesApi
import rs.edu.raf.rma.networking.model.CastResponse
import rs.edu.raf.rma.networking.model.ImageItem
import rs.edu.raf.rma.networking.model.MovieListResponse
import rs.edu.raf.rma.networking.model.VideoItem
import rs.edu.raf.rma.networking.model.MovieImagesResponse

class MoviesRepository(
    private val moviesApi: MoviesApi,
) {
    suspend fun getGenres(): List<Genre> {
        return moviesApi.getGenres()
    }

    suspend fun getMovie(id: String): MovieItem {
        return moviesApi.getMovie(id)
    }

    suspend fun getCast(id: String): CastResponse {
        return moviesApi.getCast(id)
    }

    suspend fun getVideos(id: String): List<VideoItem> {
        return moviesApi.getVideos(id, type = "Trailer")
    }

    suspend fun getImages(id: String): List<ImageItem> {
        return moviesApi.getImages(id, type = "backdrop").backdrops
    }

    suspend fun getMovies(
        query: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
        genreId: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Float? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
    ): MovieListResponse {
        return moviesApi.getMovies(
            query = query,
            page = page,
            pageSize = pageSize,
            genreId = genreId,
            minYear = minYear,
            maxYear = maxYear,
            minRating = minRating,
            sortBy = sortBy,
            sortOrder = sortOrder,
        )
    }
}
