package com.example.g_corestreamsdemo.screens

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.MediaController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.g_corestreamsdemo.R
import com.example.g_corestreamsdemo.databinding.FragmentStreamPlayerBinding
import com.example.g_corestreamsdemo.model.data.remote.RemoteAccessManager
import com.example.g_corestreamsdemo.model.data.remote.streams.stream_detailed.StreamDetailedResponse
import io.reactivex.disposables.CompositeDisposable

/**
 * If possible, check broadcast playback on physical devices,
 * MediaPlayer does not always work correctly on Android-emulators
 */
class StreamPlayerFragment : Fragment(R.layout.fragment_stream_player) {

    private lateinit var binding: FragmentStreamPlayerBinding
    private val compositeDisposable = CompositeDisposable()
    private var streamInfo: StreamDetailedResponse? = null

    private var startLoadingStreamTimeMills: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStreamPlayerBinding.bind(view)
        Log.e(myTag, "onViewCreated")

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        val streamId = requireArguments().getInt(streamId)
        getStreamDetailed(streamId)

        setSystemUiVisibilityChangeListener()
    }

    override fun onStop() {
        super.onStop()
        Log.e(myTag, "onStop")
        releasePlayer()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        hideSystemUI()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onDestroy() {
        super.onDestroy()
        Log.e(myTag, "onDestroy")
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compositeDisposable.dispose()
    }

    private fun getStreamDetailed(streamId: Int) {
        compositeDisposable.add(
            RemoteAccessManager.getStreamDetailed(requireActivity(), streamId)
                .subscribe({ streamDetailed ->
                    streamInfo = streamDetailed

                    binding.streamName.text = streamDetailed.streamName
                    startLoadingStreamTimeMills = System.currentTimeMillis()
                    initializePlayer(streamDetailed.streamHlsPlaylistUrl)
                }, {
                    streamPlaybackError()
                })
        )
    }

    private fun initializePlayer(streamUri: String) {
        val videoView = binding.videoView

        videoView.setVideoURI(Uri.parse(streamUri))
        val mediaController = MediaController(videoView.context)
        mediaController.visibility = View.GONE
        mediaController.setMediaPlayer(videoView)
        videoView.setMediaController(mediaController)
        videoView.keepScreenOn = true

        videoView.setOnPreparedListener {
            binding.progressBar.visibility = View.GONE
            videoView.start()
        }
        videoView.setOnErrorListener(mediaPlayerOnErrorListener)

        binding.streamPlayer.setOnClickListener(togglePlayback)
    }

    private fun releasePlayer() {
        binding.videoView.stopPlayback()
    }

    private val mediaPlayerOnErrorListener = MediaPlayer.OnErrorListener { _, what, extra ->

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.e(myTag, "videoView error $what $extra")
            exitPlayerIfCantPlay(extra)

            return@OnErrorListener true
        } else {
            return@OnErrorListener false
        }
    }

    private fun exitPlayerIfCantPlay(extra: Int) {
        val elapsedTime = System.currentTimeMillis() - startLoadingStreamTimeMills
        if (
            (extra == MediaPlayer.MEDIA_ERROR_IO) && (elapsedTime <= timeoutCantPlayInMillis)
        ) {
            if (streamInfo != null) {
                if (streamInfo!!.streamLive || streamInfo!!.streamBackupLive) {
                    binding.videoView.postDelayed({
                        releasePlayer()
                        initializePlayer(streamInfo!!.streamHlsPlaylistUrl)
                    }, 4000)
                }
            }
        } else {
            streamPlaybackError()
        }
    }

    private val togglePlayback = View.OnClickListener {
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
            binding.indicatorPlayback.setImageResource(R.drawable.ic_play_arrow)

            binding.indicatorPlayback.visibility = View.VISIBLE
        } else {
            binding.videoView.start()
            binding.indicatorPlayback.setImageResource(R.drawable.ic_pause)

            binding.indicatorPlayback.visibility = View.VISIBLE
            binding.indicatorPlayback.postDelayed({
                binding.indicatorPlayback.visibility = View.GONE
            }, 1000)
        }
    }

    private fun setSystemUiVisibilityChangeListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            requireActivity().window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    hideSystemUI()
                }
            }
        }
    }

    private fun streamPlaybackError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.stream_playback_error),
            Toast.LENGTH_SHORT
        ).show()
        findNavController().popBackStack()
    }

    private fun hideSystemUI() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window.setDecorFitsSystemWindows(false)
                requireActivity().window.insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                binding.videoView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window.setDecorFitsSystemWindows(true)
                requireActivity().window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                binding.videoView.systemUiVisibility = View.VISIBLE
            }
        }
    }

    companion object {
        const val streamId = "streamId"
        private const val myTag = "StreamPlayerFragment"

        private const val timeoutCantPlayInMillis = 12000
    }
}