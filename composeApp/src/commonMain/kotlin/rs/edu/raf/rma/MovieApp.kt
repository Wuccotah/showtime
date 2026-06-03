package rs.edu.raf.rma

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import rs.edu.raf.rma.auth.LoginScreen
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.movies.MoviesNavigation

@Composable
fun MovieApp() {
    val authStore = koinInject<AuthStore>()
    val authState by authStore.authState.collectAsState()

    when (authState) {
        is AuthState.Authenticated -> MoviesNavigation()
        AuthState.Unauthenticated -> LoginScreen()
    }
}
