package com.example.g_corestreamsdemo.model

import com.example.g_corestreamsdemo.model.data.remote.streams.StreamItemResponse

data class StreamItemModel(
    val streamId: Int,
    val streamName: String,
    val streamLive: Boolean,
    val streamBackupLive: Boolean,
    val streamActive: Boolean,
    val streamPushUrl: String,
    val streamBackupPushUrl: String,
    var streamPreviewUri: String? = null
) {

    companion object {

        fun getInstance(streamItem: StreamItemResponse): StreamItemModel {

            return StreamItemModel(
                streamId = streamItem.streamId,
                streamName = streamItem.streamName,
                streamLive = streamItem.streamLive,
                streamBackupLive = streamItem.streamBackupLive,
                streamActive = streamItem.streamActive,
                streamPushUrl = streamItem.streamPushUrl,
                streamBackupPushUrl = streamItem.streamBackupPushUrl,
                streamPreviewUri = streamItem.streamOverlay ?: getPosterBroadcast(streamItem)
            )
        }

        private fun getPosterBroadcast(streamItem: StreamItemResponse): String? {

            if (!streamItem.streamBroadcasts.isNullOrEmpty()) {
                return streamItem.streamBroadcasts.find { broadcast ->
                    broadcast.broadcastPoster != null
                }?.broadcastPoster
            }
            return null
        }
    }
}