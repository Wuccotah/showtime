package rs.edu.raf.rma.movies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.details.MovieDetailsScreen
import rs.edu.raf.rma.movies.details.MovieDetailsViewModel
import rs.edu.raf.rma.movies.filter.MoviesFilterScreen
import rs.edu.raf.rma.movies.filter.MoviesFilterViewModel
import rs.edu.raf.rma.movies.list.MoviesListScreen
import rs.edu.raf.rma.movies.list.MoviesListViewModel

@Composable
fun MoviesNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "movies",
    ) {
        composable(route = "movies") {
            val viewModel = koinViewModel<MoviesListViewModel>()
            MoviesListScreen(
                viewModel = viewModel,
                onMovieClick = { imdbId ->
                    navController.navigateToMovieDetails(imdbId)
                },
                onFilterClick = {
                    navController.navigate("filter")
                },
            )
        }

        composable(route = "filter") {
            val listEntry = remember { navController.getBackStackEntry("movies") }
            val listViewModel = koinViewModel<MoviesListViewModel>(viewModelStoreOwner = listEntry)
            val filterViewModel = koinViewModel<MoviesFilterViewModel>()

            MoviesFilterScreen(
                viewModel = filterViewModel,
                initialFilters = listViewModel.state.value.filters,
                onApplyFilters = { filters ->
                    listViewModel.applyFilters(filters)
                    navController.navigateUp()
                },
                onBack = {
                    navController.navigateUp()
                },
            )
        }

        composable(
            route = "movies/{$MOVIE_ID}",
            arguments = listOf(
                navArgument(MOVIE_ID) {
                    type = NavType.StringType
                    nullable = false
                }
            ),
        ) {
            val viewModel = koinViewModel<MovieDetailsViewModel>()
            MovieDetailsScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() },
            )
        }
    }
}

private fun NavController.navigateToMovieDetails(imdbId: String) {
    navigate("movies/$imdbId")
}

const val MOVIE_ID = "movieId"
inline val SavedStateHandle.movieId: String? get() = get(MOVIE_ID)
inline val SavedStateHandle.movieIdOrThrow: String get() = get(MOVIE_ID)
    ?: throw IllegalStateException("$MOVIE_ID is mandatory and cannot be null")
