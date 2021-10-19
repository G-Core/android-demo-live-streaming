package com.example.g_corestreamsdemo.screens.broadcast

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.g_corestreamsdemo.R
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtmp.flv.video.ProfileIop
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.util.BitrateAdapter
import com.pedro.rtplibrary.util.FpsListener
import com.pedro.rtplibrary.view.OpenGlView

class StartBroadcastViewModel(application: Application) : AndroidViewModel(application) {

    private val _toastMessageId = MutableLiveData<Int>()
    val toastMessageId: LiveData<Int> = _toastMessageId

    private val _streamState = MutableLiveData<StreamState>()
    val streamState: LiveData<StreamState> = _streamState

    private val _streamResolution = MutableLiveData<StreamResolution>()
    val streamResolution: LiveData<StreamResolution> = _streamResolution

    private val _currentFps = MutableLiveData<Int>()
    val currentFps: LiveData<Int> = _currentFps

    private val _currentBitrate = MutableLiveData<Int>()
    val currentBitrate = _currentBitrate

    private var rtmpCamera2: RtmpCamera2? = null
    private var isMuteMode = false

    private var appIsBackground = false
    private var startTimeInBackground = Long.MAX_VALUE

    init {
        _streamState.value = StreamState.STOP
        _streamResolution.value = StreamResolution.FULL_HD
    }

    fun createRtmpCamera2(openGlView: OpenGlView) {
        if (rtmpCamera2 == null) {
            rtmpCamera2 = RtmpCamera2(openGlView, connectCheckerRtmp).apply {
                setProfileIop(ProfileIop.CONSTRAINED)
                setFpsListener(fpsListenerCallback)
                enableAutoFocus()
            }
        }
    }

    fun startBroadcast(rtmpUrl: String) {
        rtmpCamera2?.let {
            if (!it.isStreaming) {
                if (it.prepareAudio() && it.prepareVideo(
                        StreamParameters.resolution.width,
                        StreamParameters.resolution.height,
                        StreamParameters.fps,
                        StreamParameters.startBitrate,
                        StreamParameters.iFrameIntervalInSeconds,
                        CameraHelper.getCameraOrientation(getApplication())
                    )
                ) {
                    _streamState.value = StreamState.PLAY
                    it.startStream(rtmpUrl)

                } else {
                    _streamState.value = StreamState.STOP
                    _toastMessageId.value = R.string.error_preparing_stream
                }
            }
        }
    }

    fun stopBroadcast() {
        rtmpCamera2?.let {
            if (it.isStreaming) {
                _streamState.value = StreamState.STOP
                it.stopStream()
            }
        }
    }

    fun resumeStream() {
        rtmpCamera2?.let {
            if (isMuteMode) {
                it.disableAudio()
            } else {
                it.enableAudio()
            }
            it.glInterface.unMuteVideo()
            _streamState.value = StreamState.PLAY
        }
    }

    fun pauseStream() {
        rtmpCamera2?.let {
            it.disableAudio()
            it.glInterface.muteVideo()
            _streamState.value = StreamState.PAUSE
        }
    }

    fun isFrontCamera() = rtmpCamera2?.cameraFacing == CameraHelper.Facing.FRONT

    fun switchCamera() {
        try {
            rtmpCamera2?.switchCamera()
        } catch (e: CameraOpenException) {
            Log.e(tag, e.message.toString())
        }
    }

    fun disableAudio() {
        isMuteMode = true
        rtmpCamera2?.disableAudio()
    }

    fun enableAudio() {
        isMuteMode = true
        rtmpCamera2?.enableAudio()
    }

    fun setResolution(resolution: StreamResolution) {
        rtmpCamera2?.let {
            if (!it.isStreaming) {
                StreamParameters.resolution = resolution
                _streamResolution.value = resolution

                it.stopPreview()
                it.startPreview(
                    StreamParameters.resolution.width,
                    StreamParameters.resolution.height
                )
            } else {
                _toastMessageId.value = R.string.switch_quality_failed
            }
        }
    }

    fun appInForeground(openGlView: OpenGlView) {
        appIsBackground = false

        rtmpCamera2?.let {
            it.replaceView(openGlView)
            it.startPreview(
                StreamParameters.resolution.width,
                StreamParameters.resolution.height
            )
            if (streamState.value == StreamState.PAUSE) {
                pauseStream()
            }
        }
    }

    fun appInBackground() {
        appIsBackground = true
        startTimeInBackground = System.currentTimeMillis()

        rtmpCamera2?.let {
            it.stopPreview()
            it.replaceView(getApplication() as Context)
            if (it.isStreaming) {
                pauseStream()
            }
        }
    }

    fun disableStreamingAfterTimeOut() {
        if (appIsBackground) {
            val elapsedTime = System.currentTimeMillis() - startTimeInBackground
            if (elapsedTime >= StreamParameters.backgroundStreamingTimeOutInMillis) {
                stopBroadcast()
                rtmpCamera2?.stopPreview()
            }
        }
    }

    private val fpsListenerCallback = FpsListener.Callback { fps ->
        _currentFps.postValue(fps)
    }
    private val connectCheckerRtmp = object : ConnectCheckerRtmp {

        override fun onAuthErrorRtmp() {
            _toastMessageId.postValue(R.string.auth_error)
        }

        override fun onAuthSuccessRtmp() {
            _toastMessageId.postValue(R.string.auth_success)
        }

        override fun onConnectionFailedRtmp(reason: String) {
            _toastMessageId.postValue(R.string.connection_failed)
            stopBroadcast()
        }

        override fun onConnectionStartedRtmp(rtmpUrl: String) {}

        private lateinit var bitrateAdapter: BitrateAdapter
        override fun onConnectionSuccessRtmp() {

            bitrateAdapter = BitrateAdapter { bitrate ->
                rtmpCamera2?.setVideoBitrateOnFly(bitrate)
            }.apply {
                setMaxBitrate(StreamParameters.maxBitrate)
            }

            _toastMessageId.postValue(R.string.connection_success)
            _currentBitrate.postValue(rtmpCamera2?.bitrate)
            _streamState.postValue(StreamState.PLAY)
        }

        override fun onDisconnectRtmp() {
            _toastMessageId.postValue(R.string.disconnected)
            _streamState.postValue(StreamState.STOP)
        }

        override fun onNewBitrateRtmp(bitrate: Long) {
            bitrateAdapter.adaptBitrate(bitrate)
            _currentBitrate.postValue(bitrate.toInt())

            disableStreamingAfterTimeOut()
        }
    }

    private object StreamParameters {
        var resolution = StreamResolution.FULL_HD
        const val fps = 30
        const val startBitrate = 200 * 1024
        const val iFrameIntervalInSeconds = 5
        const val maxBitrate = 3000 * 1024
        const val backgroundStreamingTimeOutInMillis: Long = 60000
    }

    override fun onCleared() {
        super.onCleared()
        stopBroadcast()
        rtmpCamera2?.stopPreview()
    }

    companion object {
        private const val tag = "StartBroadcastViewModel"
    }
}