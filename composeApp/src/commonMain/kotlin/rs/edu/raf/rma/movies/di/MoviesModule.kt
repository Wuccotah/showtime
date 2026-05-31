package rs.edu.raf.rma.movies.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.edu.raf.rma.movies.data.MovieRepository
import rs.edu.raf.rma.movies.data.MovieRepositoryImpl
import rs.edu.raf.rma.movies.details.MovieDetailsViewModel
import rs.edu.raf.rma.movies.favorites.FavoritesViewModel
import rs.edu.raf.rma.movies.filter.MoviesFilterViewModel
import rs.edu.raf.rma.movies.list.MoviesListViewModel
import rs.edu.raf.rma.movies.watchlist.WatchlistViewModel

val moviesModule = module {
    single { MovieRepositoryImpl(appDatabase = get(), moviesApi = get()) } bind MovieRepository::class
    viewModelOf(::MoviesListViewModel)
    viewModelOf(::MovieDetailsViewModel)
    viewModelOf(::MoviesFilterViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::WatchlistViewModel)
}
