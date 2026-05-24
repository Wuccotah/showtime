package rs.edu.raf.rma.networking.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import rs.edu.raf.rma.networking.MoviesApi
import rs.edu.raf.rma.networking.createMoviesApi

val networkingModule = module {

    single() {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
        json
    }

    single{
        HttpClient {
            install(ContentNegotiation) {
                json(
                    json = get<Json>()
                )
            }

            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(message, tag = "HTTP")
                    }
                }
            }
        }
    }

    single<MoviesApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>())
            .baseUrl("https://rma.finlab.rs/")
            .build()
            .createMoviesApi()
    }
    
}
