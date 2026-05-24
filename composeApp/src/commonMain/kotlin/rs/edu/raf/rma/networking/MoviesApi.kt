package rs.edu.raf.rma.networking

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import rs.edu.raf.rma.demo.Genre
import rs.edu.raf.rma.demo.MovieItem
import rs.edu.raf.rma.networking.model.CastResponse
import rs.edu.raf.rma.networking.model.MovieImagesResponse
import rs.edu.raf.rma.networking.model.MovieListResponse
import rs.edu.raf.rma.networking.model.VideoItem

interface MoviesApi {

    @GET("genres")
    suspend fun getGenres(): List<Genre>

    @GET("movies/{id}")
    suspend fun getMovie(@Path("id") id: String): MovieItem

    @GET("movies/{id}/videos")
    suspend fun getVideos(
        @Path("id") id: String,
        @Query("type") type: String? = null,
    ): List<VideoItem>

    @GET("movies/{id}/images")
    suspend fun getImages(
        @Path("id") id: String,
        @Query("type") type: String? = null,
    ): MovieImagesResponse

    @GET("movies/{id}/cast")
    suspend fun getCast(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): CastResponse

    @GET("movies")
    suspend fun getMovies(
        @Query("query") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("genre_id") genreId: Int? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Float? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
    ): MovieListResponse
}
