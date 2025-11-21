package com.example.sbooks.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast

class BroadcastWifi : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        // Kiểm tra có WiFi không
        if (!isWifiConnected(context)) {
            // Không có WiFi -> Thông báo cho user
            Toast.makeText(
                context,
                "Không có kết nối WiFi. Vui lòng kiểm tra kết nối mạng!",
                Toast.LENGTH_LONG
            ).show()
        }
        // Có WiFi -> Không làm gì cả
    }

    private fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            // Android cũ hơn
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }
}