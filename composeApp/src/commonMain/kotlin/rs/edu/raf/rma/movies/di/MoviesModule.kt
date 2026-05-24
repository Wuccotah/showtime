package rs.edu.raf.rma.movies.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.rma.movies.details.MovieDetailsViewModel
import rs.edu.raf.rma.movies.filter.MoviesFilterViewModel
import rs.edu.raf.rma.movies.list.MoviesListViewModel
import rs.edu.raf.rma.movies.repository.MoviesRepository

val moviesModule = module {
    single { MoviesRepository(get()) }
    viewModelOf(::MoviesListViewModel)
    viewModelOf(::MovieDetailsViewModel)
    viewModelOf(::MoviesFilterViewModel)
}
