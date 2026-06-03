package rs.edu.raf.rma.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.core.auth.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private fun setState(reducer: LoginState.() -> LoginState) {
        _state.getAndUpdate(reducer)
    }

    fun setEvent(event: LoginEvent) {
        viewModelScope.launch {
            when (event) {
                is LoginEvent.SwitchMode -> setState {
                    copy(
                        mode = if (mode == LoginMode.Login) LoginMode.Register else LoginMode.Login,
                        error = null,
                    )
                }
                is LoginEvent.SetFullName -> setState { copy(fullName = event.value) }
                is LoginEvent.SetUsername -> setState { copy(username = event.value) }
                is LoginEvent.SetPassword -> setState { copy(password = event.value) }
                is LoginEvent.Submit -> submit()
            }
        }
    }

    private suspend fun submit() {
        val current = _state.value
        if (current.isLoading) return

        setState { copy(isLoading = true, error = null) }

        runCatching {
            when (current.mode) {
                LoginMode.Login -> authRepository.login(
                    username = current.username.trim(),
                    password = current.password,
                )
                LoginMode.Register -> authRepository.register(
                    fullName = current.fullName.trim(),
                    username = current.username.trim(),
                    password = current.password,
                )
            }
        }.onFailure { err ->
            val message = when {
                err is ClientRequestException && err.response.status == HttpStatusCode.Unauthorized ->
                    "Invalid username or password"
                err is ClientRequestException && err.response.status == HttpStatusCode.Conflict ->
                    "Username is already taken"
                err is ClientRequestException && err.response.status == HttpStatusCode.BadRequest ->
                    "Invalid input — check your fields"
                else -> "Network error, please try again"
            }
            setState { copy(error = message) }
        }

        setState { copy(isLoading = false) }
    }
}
