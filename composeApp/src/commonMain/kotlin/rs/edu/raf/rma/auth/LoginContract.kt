package rs.edu.raf.rma.auth

data class LoginState(
    val mode: LoginMode = LoginMode.Login,
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

enum class LoginMode { Login, Register }

sealed class LoginEvent {
    data object SwitchMode : LoginEvent()
    data class SetFullName(val value: String) : LoginEvent()
    data class SetUsername(val value: String) : LoginEvent()
    data class SetPassword(val value: String) : LoginEvent()
    data object Submit : LoginEvent()
}
