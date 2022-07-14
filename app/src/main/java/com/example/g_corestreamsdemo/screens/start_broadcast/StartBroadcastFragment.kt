package com.example.g_corestreamsdemo.screens.start_broadcast

import android.content.DialogInterface
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.g_corestreamsdemo.R
import com.example.g_corestreamsdemo.databinding.FragmentStartBroadcastBinding
import com.example.g_corestreamsdemo.utils.StreamResolution
import com.example.g_corestreamsdemo.utils.StreamState
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StartBroadcastFragment : Fragment(R.layout.fragment_start_broadcast) {

    private lateinit var binding: FragmentStartBroadcastBinding
    private val viewModel: StartBroadcastViewModel by lazy {
        ViewModelProvider(this).get(StartBroadcastViewModel::class.java)
    }

    private lateinit var rtmpPushUrl: String
    private lateinit var rtmpBackupPushUrl: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStartBroadcastBinding.bind(view)

        rtmpPushUrl = requireArguments().getString(rtmpPushUrlKey) ?: ""
        rtmpBackupPushUrl = requireArguments().getString(rtmpBackupPushUrlKey) ?: ""

        configureItemsDisplay()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (viewModel.streamState.value != StreamState.STOP) {
                showEndBroadcastDialog()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun configureItemsDisplay() {
        binding.openGlView.holder.addCallback(surfaceHolderCallback)
        viewModel.createRtmpCamera2(binding.openGlView)
        binding.openGlView.visibility = View.VISIBLE

        setListenersToToggleResolution()
        configureSwitchUrl()

        binding.toggleBroadcast.setOnClickListener(toggleBroadcastClick)
        binding.switchCamera.setOnClickListener(switchCameraClick)
        binding.muteToggle.setOnCheckedChangeListener(muteToggleClick)

        binding.playbackBroadcast.setOnClickListener {
            if (viewModel.streamState.value == StreamState.PLAY) {
                viewModel.pauseStream()
            } else if (viewModel.streamState.value == StreamState.PAUSE) {
                viewModel.resumeStream()
            }
        }

        listenStreamState()
        listenStreamResolution()
        listenFpsAndBitrate()
        listenMessages()
    }

    private fun configureSwitchUrl() {
        binding.pushUrlRadioButton.setOnClickListener {
            val isCameraStreaming = viewModel.streamState.value != StreamState.STOP

            if (!it.isActivated && isCameraStreaming) {
                showToastMessage(R.string.switch_url_failed)
                binding.urlGroup.check(binding.backupPushUrlRadioButton.id)
            }
        }
        binding.backupPushUrlRadioButton.setOnClickListener {
            val isCameraStreaming = viewModel.streamState.value != StreamState.STOP

            if (!it.isActivated && isCameraStreaming) {
                showToastMessage(R.string.switch_url_failed)
                binding.urlGroup.check(binding.pushUrlRadioButton.id)
            }
        }
    }

    private val toggleBroadcastClick = View.OnClickListener {

        if (viewModel.streamState.value == StreamState.STOP) {
            if (binding.pushUrlRadioButton.isChecked) {
                viewModel.startBroadcast(rtmpPushUrl)
            }
            if (binding.backupPushUrlRadioButton.isChecked) {
                viewModel.startBroadcast(rtmpBackupPushUrl)
            }
        } else {
            viewModel.stopBroadcast()
        }
    }

    private val switchCameraClick = View.OnClickListener {
        val switchCameraCounterclockwiseAnim = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_counterclockwise_180
        )
        val switchCameraClockwiseAnim = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_clockwise_180
        )

        if (viewModel.isFrontCamera()) {
            binding.switchCamera.startAnimation(switchCameraClockwiseAnim)
        } else {
            binding.switchCamera.startAnimation((switchCameraCounterclockwiseAnim))
        }

        viewModel.switchCamera()
    }

    private val muteToggleClick = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            viewModel.disableAudio()
        } else {
            viewModel.enableAudio()
        }
    }

    private fun listenStreamState() = viewModel.streamState
        .observe(viewLifecycleOwner) { streamState ->
            when (streamState!!) {
                StreamState.PLAY -> {
                    binding.playbackBroadcast.visibility = View.VISIBLE
                    binding.playbackBroadcast.setImageResource(R.drawable.ic_pause_outline)
                    binding.toggleBroadcast.setText(R.string.stop_broadcast)
                }
                StreamState.PAUSE -> {
                    binding.playbackBroadcast.setImageResource(R.drawable.ic_play_outline)
                }
                StreamState.STOP -> {
                    binding.playbackBroadcast.setImageResource(R.drawable.ic_play_outline)
                    binding.playbackBroadcast.visibility = View.GONE
                    binding.toggleBroadcast.setText(R.string.start_broadcast)
                }
            }
        }

    private fun setListenersToToggleResolution() {
        binding.fullHdQualityTV.setOnClickListener {
            viewModel.setResolution(StreamResolution.FULL_HD)
        }
        binding.hdQualityTV.setOnClickListener {
            viewModel.setResolution(StreamResolution.HD)
        }
        binding.sdQualityTV.setOnClickListener {
            viewModel.setResolution(StreamResolution.SD)
        }
    }

    private fun listenStreamResolution() = viewModel.streamResolution
        .observe(viewLifecycleOwner) { resolution ->
            binding.fullHdQualityTV.setTextColor(getColor(requireContext(), R.color.white))
            binding.hdQualityTV.setTextColor(getColor(requireContext(), R.color.white))
            binding.sdQualityTV.setTextColor(getColor(requireContext(), R.color.white))

            when (resolution!!) {
                StreamResolution.FULL_HD -> {
                    binding.fullHdQualityTV.setTextColor(getColor(requireContext(), R.color.orange))
                }
                StreamResolution.HD -> {
                    binding.hdQualityTV.setTextColor(getColor(requireContext(), R.color.orange))
                }
                StreamResolution.SD -> {
                    binding.sdQualityTV.setTextColor(getColor(requireContext(), R.color.orange))
                }
            }
        }

    private fun listenFpsAndBitrate() {
        viewModel.currentFps.observe(viewLifecycleOwner) { fps ->
            binding.currentFPS.text = getString(R.string.current_fps, fps)
        }
        viewModel.currentBitrate.observe(viewLifecycleOwner) { bitrate ->
            binding.currentBitrate.text =
                getString(R.string.current_bitrate_in_Kbps, bitrate / 1024)
        }
    }

    private fun listenMessages() = viewModel.toastMessageId
        .observe(viewLifecycleOwner) { messageId ->
            showToastMessage(messageId)
        }

    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            viewModel.appInForeground(binding.openGlView)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            viewModel.appInBackground()
        }
    }

    private fun showEndBroadcastDialog() {
        val dialogButton = DialogInterface.OnClickListener { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                findNavController().popBackStack()
            }
            dialog.dismiss()
        }
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle(R.string.end_broadcast)
            .setMessage(R.string.end_broadcast_message)
            .setPositiveButton(R.string.exit, dialogButton)
            .setNegativeButton(R.string.stay, dialogButton)
            .create()
            .show()
    }

    private fun showToastMessage(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val rtmpPushUrlKey = "rtmpPushUrl"
        const val rtmpBackupPushUrlKey = "rtmpBackupPushUrl"
    }
}