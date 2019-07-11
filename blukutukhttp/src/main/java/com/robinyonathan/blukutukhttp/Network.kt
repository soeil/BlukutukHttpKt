package com.robinyonathan.blukutukhttp

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager

@Suppress("UNNECESSARY_SAFE_CALL")
internal object Network {
    fun isNetworkAvailable(activity: Activity?): Boolean {
        var connected = false

        activity?.let {
            val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager?.let {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                activeNetworkInfo?.let {
                    connected = activeNetworkInfo.isConnected
                }
            }
        }

        return connected
    }
}
