package rs.edu.raf.rma.core.auth

interface AuthRepository {
    suspend fun login(username: String, password: String)
    suspend fun register(fullName: String, username: String, password: String)
    suspend fun logout()
}
