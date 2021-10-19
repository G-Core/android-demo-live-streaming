package com.example.g_corestreamsdemo.screens.broadcast

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.g_corestreamsdemo.R
import com.example.g_corestreamsdemo.databinding.FragmentCreateBroadcastBinding
import com.example.g_corestreamsdemo.databinding.StreamNameInputBinding
import com.example.g_corestreamsdemo.model.StreamItemModel
import com.example.g_corestreamsdemo.model.data.remote.RemoteAccessManager
import com.example.g_corestreamsdemo.model.data.remote.auth.AuthResponse
import com.example.g_corestreamsdemo.model.data.remote.streams.StreamItemResponse
import com.example.g_corestreamsdemo.screens.streams.StreamItemsAdapter
import com.example.g_corestreamsdemo.screens.streams.StreamItemsAdapterListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.disposables.CompositeDisposable

class CreateBroadcastFragment : Fragment(R.layout.fragment_create_broadcast),
    StreamItemsAdapterListener {

    private lateinit var binding: FragmentCreateBroadcastBinding

    private val streamItemsAdapter = StreamItemsAdapter()
    private val streamItems: MutableList<StreamItemModel> = ArrayList()
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreateBroadcastBinding.bind(view)

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
        binding.createStreamBtn.setOnClickListener { showCreateStreamDialog() }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
                binding.createStreamBtn.hide()
            } else {
                binding.createStreamBtn.show()
            }
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
                        updateStreamItems(it)
                        loadStreamItems(page = ++currentPage)
                    } else {
                        binding.createStreamBtn.isEnabled = true
                        updateDataInAdapter()
                    }
                }, {
                    refreshAccessToken()
                })
        )
    }

    private fun updateStreamItems(streamItemsResponse: List<StreamItemResponse>) {
        for (streamItemResponse in streamItemsResponse) {
            if (streamItemResponse.streamActive &&
                !(streamItemResponse.streamLive || streamItemResponse.streamBackupLive)
            ) {
                streamItems.add(StreamItemModel.getInstance(streamItemResponse))
            }
        }
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

    private fun updateTokens(authResponse: AuthResponse) {
        requireContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
            .edit()
            .putString(RemoteAccessManager.REFRESH_TOKEN_KEY, authResponse.refreshAccessToken)
            .putString(RemoteAccessManager.ACCESS_TOKEN_KEY, authResponse.accessToken)
            .apply()
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

    private fun showCreateStreamDialog() {
        val dialogBinding = StreamNameInputBinding.inflate(layoutInflater)
        val dialog =
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
                .setTitle(getString(R.string.create_stream))
                .setView(dialogBinding.root)
                .setPositiveButton(getString(R.string.create), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()

        dialog.setOnShowListener {
            dialogBinding.etStreamName.requestFocus()
            showKeyboard(dialogBinding.etStreamName)

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val enteredName = dialogBinding.etStreamName.text.toString()
                if (enteredName.isBlank()) {
                    dialogBinding.etStreamName.error = getString(R.string.stream_name_is_empty)
                    return@setOnClickListener
                }
                createStream(enteredName)
                dialog.dismiss()
            }
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setOnClickListener { dialog.dismiss() }
        }
        dialog.setOnDismissListener { hideKeyBoard(dialogBinding.etStreamName) }
        dialog.show()
    }

    private fun createStream(streamName: String) {
        binding.refresherList.isRefreshing = true

        compositeDisposable.add(
            RemoteAccessManager.createStream(requireActivity(), streamName)
                .subscribe({
                    loadStreamItems()
                }, {
                    binding.refresherList.isRefreshing = false
                    Toast.makeText(
                        requireContext(),
                        R.string.create_stream_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                })
        )
    }

    override fun onItemClick(position: Int) {
        val streamItem = streamItems[position]

        findNavController().navigate(
            R.id.action_createBroadcastFragment_to_startBroadcastFragment,
            bundleOf(
                StartBroadcastFragment.rtmpPushUrlKey to streamItem.streamPushUrl,
                StartBroadcastFragment.rtmpBackupPushUrlKey to streamItem.streamBackupPushUrl
            ),
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

    private fun showKeyboard(view: View) {
        view.post {
            getInputMethodManager(view).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyBoard(view: View) {
        getInputMethodManager(view).hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun getInputMethodManager(view: View): InputMethodManager {
        val context = view.context
        return context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
}