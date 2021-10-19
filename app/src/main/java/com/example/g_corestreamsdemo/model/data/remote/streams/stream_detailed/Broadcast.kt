package com.example.g_corestreamsdemo.model.data.remote.streams.stream_detailed

import com.google.gson.annotations.SerializedName

data class Broadcast(
    @SerializedName("id") val broadcastId: Int,
    @SerializedName("name") val broadcastName: String,
    @SerializedName("client_id") val clientId: Int,
    @SerializedName("status") val broadcastStatus: String,
    @SerializedName("iframe_embed_code") val broadcastIframeEmbedCode: String?,
    @SerializedName("share_url") val broadcastShareUrl: String?,
    @SerializedName("custom_iframe_url") val broadcastCustomIframeUrl: String?,
    @SerializedName("iframe_url") val broadcastIframeUrl: String?,
    @SerializedName("show_dvr_after_finish") val broadcastShowDvrAfterFinish: Boolean,
    @SerializedName("pending_message") val broadcastPendingMessage: String?,
    @SerializedName("custom_messages") val broadcastCustomMessage: String?,
    @SerializedName("ad_id") val broadcastAdId: Int?,
    @SerializedName("player_id") val broadcastPlayerId: Int?,
    @SerializedName("client_user_id") val broadcastClientUserId: Int?,
    @SerializedName("poster_thumb") val broadcastPosterThumb: String?,
    @SerializedName("stream_ids") val broadcastStreamIds: List<Int>,
    @SerializedName("poster") val broadcastPoster: String?
)