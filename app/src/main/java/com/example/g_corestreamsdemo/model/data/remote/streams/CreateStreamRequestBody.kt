package com.example.g_corestreamsdemo.model.data.remote.streams

import com.google.gson.annotations.SerializedName

class CreateStreamRequestBody(
    @SerializedName("stream") val stream: StreamToBeCreated
)

