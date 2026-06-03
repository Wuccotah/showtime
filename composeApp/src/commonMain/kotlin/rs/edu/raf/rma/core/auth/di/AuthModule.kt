package rs.edu.raf.rma.core.auth.di

import androidx.datastore.core.DataStore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.edu.raf.rma.auth.LoginViewModel
import rs.edu.raf.rma.core.auth.AuthRepository
import rs.edu.raf.rma.core.auth.AuthRepositoryImpl
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.createAuthDataStore
import rs.edu.raf.rma.core.auth.model.AuthData

val authModule = module {

    single<DataStore<AuthData>> { createAuthDataStore() }

    single<AuthStore> { AuthStore(persistence = get()) }

    single { AuthRepositoryImpl(authApi = get(), authStore = get(), movieRepository = get()) } bind AuthRepository::class

    viewModelOf(::LoginViewModel)
}
