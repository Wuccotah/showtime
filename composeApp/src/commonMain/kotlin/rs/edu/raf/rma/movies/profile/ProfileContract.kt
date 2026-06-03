package rs.edu.raf.rma.profile

data class ProfileState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val username: String = "",
    val fullName: String = "",
    val favoritesCount: Int = 0,
    val watchlistCount: Int = 0,
    val bestScore: Float? = null,
    val quizCount: Int = 0,
)

sealed class ProfileEvent {
    data object Logout : ProfileEvent()
    data object Retry : ProfileEvent()
}

sealed class ProfileSideEffect {
    data object LoggedOut : ProfileSideEffect()
}
