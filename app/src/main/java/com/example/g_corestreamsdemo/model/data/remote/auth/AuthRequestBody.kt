package com.example.g_corestreamsdemo.model.data.remote.auth

import com.google.gson.annotations.SerializedName

class AuthRequestBody(
    @SerializedName("username") val eMail: String,
    @SerializedName("password") val password: String,
    @SerializedName("one_time_password") val oneTimePassword: String = "authenticator passcode"
)