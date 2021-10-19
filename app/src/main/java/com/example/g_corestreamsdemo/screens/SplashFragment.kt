package com.example.g_corestreamsdemo.screens

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.g_corestreamsdemo.R
import com.example.g_corestreamsdemo.model.data.remote.RemoteAccessManager

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (RemoteAccessManager.isAuth(requireActivity())) {
            routeTo(R.id.action_splashFragment_to_streamsFragment)
        } else {
            routeTo(R.id.action_splashFragment_to_loginFragment)
        }

        Log.e("SplashFragment", "onViewCreated}")
    }

    private fun routeTo(@IdRes actionId: Int) = findNavController().navigate(
        actionId,
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
}