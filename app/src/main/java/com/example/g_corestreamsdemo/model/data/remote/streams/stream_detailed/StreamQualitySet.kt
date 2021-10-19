package com.example.g_corestreamsdemo.model.data.remote.streams.stream_detailed

import com.google.gson.annotations.SerializedName

data class StreamQualitySet(
    @SerializedName("type") val streamType: String?,
    @SerializedName("qualities") val streamQualities: List<StreamQuality>
)