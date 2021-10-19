package com.example.g_corestreamsdemo.model.data.remote.refresh_token

import com.example.g_corestreamsdemo.model.data.remote.auth.AuthResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshTokenApi {
    @POST("./auth/jwt/refresh")
    fun refreshToken(@Body body: RefreshRequestBody): Single<AuthResponse>
}