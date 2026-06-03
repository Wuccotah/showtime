package rs.edu.raf.rma.networking.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.SetupRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.networking.AuthApi
import rs.edu.raf.rma.networking.MoviesApi
import rs.edu.raf.rma.networking.createAuthApi
import rs.edu.raf.rma.networking.createMoviesApi

private const val BASE_URL = "https://rma.finlab.rs/"

val networkingModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }

    single<HttpClient>(Qualifiers.Unauthenticated) {
        buildHttpClient(get())
    }

    single<HttpClient>(Qualifiers.Authenticated) {
        val authStore = get<AuthStore>()
        buildHttpClient(get()) {
            installAuthPlugin(authStore)
        }
    }

    single<AuthApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Unauthenticated))
            .baseUrl(BASE_URL)
            .build()
            .createAuthApi()
    }

    single<MoviesApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl(BASE_URL)
            .build()
            .createMoviesApi()
    }
}

private fun buildHttpClient(
    json: Json,
    block: HttpClientConfig<*>.() -> Unit = {},
) = HttpClient {
    expectSuccess = true
    install(ContentNegotiation) { json(json) }
    defaultRequest { contentType(ContentType.Application.Json) }
    install(Logging) {
        level = LogLevel.ALL
        logger = object : Logger {
            override fun log(message: String) {
                Napier.d(message, tag = "HTTP")
            }
        }
    }
    block()
}

private fun HttpClientConfig<*>.installAuthPlugin(
    authStore: AuthStore,
) = install(createClientPlugin("AuthPlugin") {
    on(SetupRequest) { request ->
        when (val state = authStore.authState.value) {
            is AuthState.Authenticated ->
                request.header(HttpHeaders.Authorization, "Bearer ${state.data.accessToken}")
            AuthState.Unauthenticated -> Unit
        }
    }
})
