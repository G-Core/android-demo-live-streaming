package com.example.g_corestreamsdemo.model.data.remote.auth

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("refresh") val refreshAccessToken: String,
    @SerializedName("access") val accessToken: String
)