package com.example.g_corestreamsdemo.model.data.remote.streams

import com.google.gson.annotations.SerializedName

class StreamToBeCreated(
    @SerializedName("name") val streamName: String,
    @SerializedName("pull") val streamPull: Boolean = false,
    @SerializedName("active") val streamActive: Boolean = true
)