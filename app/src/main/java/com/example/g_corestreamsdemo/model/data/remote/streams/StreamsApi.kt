package com.example.g_corestreamsdemo.model.data.remote.streams

import com.example.g_corestreamsdemo.model.data.remote.streams.stream_detailed.StreamDetailedResponse
import io.reactivex.Single
import retrofit2.http.*

interface StreamsApi {

    /**
     * @param page integer; Query parameter. Use it to list the paginated content
     * @param with_broadcasts integer; Query parameter.
     * Set to 1 to get details of the broadcasts associated with the stream
     */
    @GET("./vp/api/streams")
    fun getStreams(
        @Header("Authorization") accessToken: String,
        @Query("page") page: Int,
        @Query("with_broadcasts") withBroadcasts: Int = 1
    ): Single<List<StreamItemResponse>>

    @GET("/vp/api/streams/{stream_id}")
    fun getStreamDetailed(
        @Header("Authorization") accessToken: String,
        @Path("stream_id") streamId: Int
    ): Single<StreamDetailedResponse>

    @POST("./vp/api/streams")
    fun createStream(
        @Header("Authorization") accessToken: String,
        @Body body: CreateStreamRequestBody
    ): Single<StreamItemResponse>
}