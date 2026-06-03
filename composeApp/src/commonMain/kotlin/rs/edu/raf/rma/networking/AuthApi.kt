package rs.edu.raf.rma.networking

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import rs.edu.raf.rma.networking.model.AuthResponse
import rs.edu.raf.rma.networking.model.LoginBody
import rs.edu.raf.rma.networking.model.RegisterBody

interface AuthApi {

    @POST("auth/signup")
    suspend fun register(@Body body: RegisterBody): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginBody): AuthResponse
}
