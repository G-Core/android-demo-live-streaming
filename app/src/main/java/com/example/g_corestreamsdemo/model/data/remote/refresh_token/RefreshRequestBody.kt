package com.example.g_corestreamsdemo.model.data.remote.refresh_token

import com.google.gson.annotations.SerializedName

class RefreshRequestBody(
    @SerializedName("refresh") val refreshAccessToken: String
)