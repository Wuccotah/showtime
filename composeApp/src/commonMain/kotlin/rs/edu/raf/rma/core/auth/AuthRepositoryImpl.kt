package rs.edu.raf.rma.core.auth

import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.movies.data.MovieRepository
import rs.edu.raf.rma.networking.AuthApi
import rs.edu.raf.rma.networking.model.LoginBody
import rs.edu.raf.rma.networking.model.RegisterBody

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val authStore: AuthStore,
    private val movieRepository: MovieRepository,
) : AuthRepository {

    override suspend fun login(username: String, password: String) {
        val response = authApi.login(LoginBody(username, password))
        authStore.setAuthData(AuthData(accessToken = response.accessToken))
    }

    override suspend fun register(fullName: String, username: String, password: String) {
        val response = authApi.register(RegisterBody(fullName, username, password))
        authStore.setAuthData(AuthData(accessToken = response.accessToken))
    }

    override suspend fun logout() {
        authStore.clearAuthData()
        movieRepository.clearUserData()
    }
}
