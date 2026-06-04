package rs.edu.raf.rma.movies

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.details.MovieDetailsScreen
import rs.edu.raf.rma.movies.details.MovieDetailsViewModel
import rs.edu.raf.rma.movies.favorites.FavoritesScreen
import rs.edu.raf.rma.movies.favorites.FavoritesViewModel
import rs.edu.raf.rma.movies.filter.MoviesFilterScreen
import rs.edu.raf.rma.movies.filter.MoviesFilterViewModel
import rs.edu.raf.rma.movies.list.MoviesListScreen
import rs.edu.raf.rma.movies.list.MoviesListViewModel
import rs.edu.raf.rma.movies.watchlist.WatchlistScreen
import rs.edu.raf.rma.movies.watchlist.WatchlistViewModel
import rs.edu.raf.rma.profile.ProfileScreen
import rs.edu.raf.rma.profile.ProfileViewModel
import rs.edu.raf.rma.quiz.QuizResultScreen
import rs.edu.raf.rma.quiz.QuizScreen
import rs.edu.raf.rma.quiz.QuizViewModel

private val BgDark = Color(0xFF14181C)
private val BgSurface = Color(0xFF22272E)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8899AA)
private val AccentGreen = Color(0xFF00E054)

private enum class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Movies("movies", "Movies", Icons.Filled.Home, Icons.Outlined.Home),
    Favorites("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    Watchlist("watchlist", "Watchlist", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    Quiz("quiz", "Quiz", Icons.Filled.Quiz, Icons.Outlined.Quiz),
    Profile("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person),
}

private val rootRoutes = BottomTab.entries.map { it.route }.toSet()

@Composable
fun MoviesNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in rootRoutes

    var pendingTab by remember { mutableStateOf<BottomTab?>(null) }

    if (pendingTab != null) {
        AlertDialog(
            onDismissRequest = { pendingTab = null },
            title = { Text("Abandon quiz?", color = Color(0xFFFFFFFF)) },
            text = { Text("Your progress will be lost.", color = Color(0xFF8899AA)) },
            confirmButton = {
                TextButton(onClick = {
                    val target = pendingTab!!
                    pendingTab = null
                    navController.navigate(target.route) {
                        popUpTo("movies") { saveState = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Text("Leave", color = Color(0xFFE74C3C))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingTab = null }) {
                    Text("Keep playing", color = Color(0xFF00E054))
                }
            },
            containerColor = Color(0xFF22272E),
        )
    }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = BgSurface) {
                    BottomTab.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    if (currentRoute == BottomTab.Quiz.route) {
                                        pendingTab = tab
                                    } else if (tab == BottomTab.Quiz) {
                                        navController.navigate("quiz") {
                                            popUpTo("movies") { saveState = true }
                                            launchSingleTop = false
                                            restoreState = false
                                        }
                                    } else {
                                        navController.navigate(tab.route) {
                                            popUpTo("movies") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label,
                                )
                            },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentGreen,
                                selectedTextColor = AccentGreen,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BgSurface,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "movies",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(route = "movies") {
                val viewModel = koinViewModel<MoviesListViewModel>()
                MoviesListScreen(
                    viewModel = viewModel,
                    onMovieClick = { navController.navigateToMovieDetails(it) },
                    onFilterClick = { navController.navigate("filter") },
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
                    onBack = { navController.navigateUp() },
                )
            }

            composable(
                route = "movies/{$MOVIE_ID}",
                arguments = listOf(navArgument(MOVIE_ID) { type = NavType.StringType }),
            ) {
                val viewModel = koinViewModel<MovieDetailsViewModel>()
                MovieDetailsScreen(
                    viewModel = viewModel,
                    onBack = { navController.navigateUp() },
                )
            }

            composable(route = "favorites") {
                val viewModel = koinViewModel<FavoritesViewModel>()
                FavoritesScreen(
                    viewModel = viewModel,
                    onMovieClick = { navController.navigateToMovieDetails(it) },
                )
            }

            composable(route = "watchlist") {
                val viewModel = koinViewModel<WatchlistViewModel>()
                WatchlistScreen(
                    viewModel = viewModel,
                    onMovieClick = { navController.navigateToMovieDetails(it) },
                )
            }

            composable(route = "quiz") {
                val viewModel = koinViewModel<QuizViewModel>()
                QuizScreen(
                    viewModel = viewModel,
                    onNavigateToResult = { score, correct, total, timeUsed ->
                        navController.navigate("quiz/result/$score/$correct/$total/$timeUsed") {
                            popUpTo("quiz") { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.navigate("movies") {
                            popUpTo("quiz") { inclusive = true; saveState = false }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(
                route = "quiz/result/{score}/{correct}/{total}/{timeUsed}",
                arguments = listOf(
                    navArgument("score") { type = NavType.FloatType },
                    navArgument("correct") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType },
                    navArgument("timeUsed") { type = NavType.IntType },
                ),
            ) { backStack ->
                val score = backStack.arguments?.getFloat("score") ?: 0f
                val correct = backStack.arguments?.getInt("correct") ?: 0
                val total = backStack.arguments?.getInt("total") ?: 0
                val timeUsed = backStack.arguments?.getInt("timeUsed") ?: 0
                QuizResultScreen(
                    score = score,
                    correctCount = correct,
                    totalQuestions = total,
                    timeUsedSeconds = timeUsed,
                    onDone = {
                        navController.navigate("quiz") {
                            popUpTo("quiz") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(route = "profile") {
                val viewModel = koinViewModel<ProfileViewModel>()
                ProfileScreen(viewModel = viewModel)
            }
        }
    }
}

private fun NavController.navigateToMovieDetails(imdbId: String) {
    navigate("movies/$imdbId")
}

const val MOVIE_ID = "movieId"
inline val SavedStateHandle.movieId: String? get() = get(MOVIE_ID)
inline val SavedStateHandle.movieIdOrThrow: String
    get() = get(MOVIE_ID) ?: throw IllegalStateException("$MOVIE_ID is mandatory and cannot be null")
