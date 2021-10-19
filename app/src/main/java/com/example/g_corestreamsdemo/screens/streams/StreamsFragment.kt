package com.example.g_corestreamsdemo.screens.streams

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.g_corestreamsdemo.R
import com.example.g_corestreamsdemo.databinding.FragmentStreamsBinding
import com.example.g_corestreamsdemo.model.StreamItemModel
import com.example.g_corestreamsdemo.model.data.remote.RemoteAccessManager
import com.example.g_corestreamsdemo.model.data.remote.auth.AuthResponse
import com.example.g_corestreamsdemo.screens.StreamPlayerFragment
import io.reactivex.disposables.CompositeDisposable

class StreamsFragment : Fragment(R.layout.fragment_streams), StreamItemsAdapterListener {

    private lateinit var binding: FragmentStreamsBinding

    private val streamItemsAdapter = StreamItemsAdapter()
    private val streamItems: MutableList<StreamItemModel> = ArrayList()
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStreamsBinding.bind(view)

        streamItemsAdapter.setListener(this)
        binding.streamsRecyclerView.adapter = streamItemsAdapter
        binding.streamsRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.streamsRecyclerView.addOnScrollListener(recyclerViewScrollListener)

        binding.refresherList.isRefreshing = true
        binding.refresherList.setOnRefreshListener { loadStreamItems() }
        loadStreamItems()

        checkCameraAccess()
        binding.createBroadcastBtn.setOnClickListener { getCameraPermissions() }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun checkCameraAccess() {
        if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            binding.createBroadcastBtn.isEnabled = true
            binding.createBroadcastBtn.show()
        } else {
            binding.createBroadcastBtn.isEnabled = false
            binding.createBroadcastBtn.hide()
        }
    }

    private val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
                binding.createBroadcastBtn.hide()
            } else {
                binding.createBroadcastBtn.show()
            }
        }
    }

    private fun getCameraPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        )
        val audioPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        )

        if (cameraPermission == PackageManager.PERMISSION_DENIED ||
            audioPermission == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionsCamera.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        } else {
            routeToCreateBroadcast()
        }
    }

    private val requestPermissionsCamera =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsState ->
            var allPermissionsGranted = true
            for (isGranted in permissionsState.values) {
                if (!isGranted) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.camera_or_record_audio_is_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                routeToCreateBroadcast()
            }
        }

    private fun loadStreamItems(page: Int = 1) {
        var currentPage = page
        if (currentPage == 1) {
            streamItems.clear()
        }

        compositeDisposable.add(
            RemoteAccessManager.loadStreamItems(requireActivity(), currentPage)
                .subscribe({
                    if (it.isNotEmpty()) {
                        it.forEach { streamItemResponse ->
                            streamItems.add(StreamItemModel.getInstance(streamItemResponse))
                        }
                        loadStreamItems(page = ++currentPage)
                    } else {
                        updateDataInAdapter()
                    }
                }, {
                    refreshAccessToken()
                })
        )
    }

    private fun updateDataInAdapter() {
        streamItemsAdapter.setData(streamItems)
        binding.refresherList.isRefreshing = false

        if (streamItems.isEmpty()) {
            binding.emptyListTextView.visibility = View.VISIBLE
        } else {
            binding.emptyListTextView.visibility = View.GONE
        }
    }

    private fun refreshAccessToken() {
        compositeDisposable.add(
            RemoteAccessManager.refreshAccessToken(requireActivity())
                .subscribe({
                    updateTokens(it)
                    loadStreamItems()
                }, {
                    auth()
                })
        )
    }

    private fun auth() {
        compositeDisposable.add(
            RemoteAccessManager.auth(requireActivity())
                .subscribe({
                    updateTokens(it)
                    loadStreamItems()
                }, {
                    routeToLoginFragment()
                })
        )
    }

    private fun routeToLoginFragment() = findNavController().navigate(
        R.id.loginFragment,
        null,
        navOptions {
            anim {
                enter = R.anim.enter_fragment
                exit = R.anim.exit_fragment
                popEnter = R.anim.pop_enter_fragment
                popExit = R.anim.pop_exit_fragment
            }
            launchSingleTop = true
            popUpTo(R.id.nav_graph_app) { inclusive = true }
        }
    )

    private fun updateTokens(authResponse: AuthResponse) {
        requireContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
            .edit()
            .putString(RemoteAccessManager.REFRESH_TOKEN_KEY, authResponse.refreshAccessToken)
            .putString(RemoteAccessManager.ACCESS_TOKEN_KEY, authResponse.accessToken)
            .apply()
    }

    override fun onItemClick(position: Int) {
        val streamItem = streamItems[position]

        if ((streamItem.streamLive || streamItem.streamBackupLive) && streamItem.streamActive) {
            routeToPlayer(streamItem)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.stream_not_available),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun routeToPlayer(streamItem: StreamItemModel) = findNavController().navigate(
        R.id.action_streamsFragment_to_streamPlayerFragment,
        bundleOf(StreamPlayerFragment.streamId to streamItem.streamId),
        navOptions {
            anim {
                enter = R.anim.enter_fragment
                exit = R.anim.exit_fragment
                popEnter = R.anim.pop_enter_fragment
                popExit = R.anim.pop_exit_fragment
            }
        }
    )

    private fun routeToCreateBroadcast() = findNavController().navigate(
        R.id.action_streamsFragment_to_createBroadcastFragment,
        null,
        navOptions {
            anim {
                enter = R.anim.enter_fragment
                exit = R.anim.exit_fragment
                popEnter = R.anim.pop_enter_fragment
                popExit = R.anim.pop_exit_fragment
            }
        }
    )
}