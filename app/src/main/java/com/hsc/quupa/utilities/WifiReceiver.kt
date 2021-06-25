package com.hsc.quupa.utilities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import com.hsc.quupa.listener.OnWifiChanged

class WifiReceiver: BroadcastReceiver() {

    private lateinit var userData: UserPreference

    companion object {
        private lateinit var listener: OnWifiChanged
        fun bindListener(listener: OnWifiChanged) {
            this.listener = listener
        }
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        userData = UserPreference(context!!)
        val info = intent?.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
        if (info != null && info.isConnected) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ip = wifiInfo.ipAddress
            val ipAddress = String.format(
                "%d.%d.%d.",
                ip and 0xff,
                ip shr 8 and 0xff,
                ip shr 16 and 0xff
            )

            userData.setIP(ipAddress)
            listener.onWifiChanged(ipAddress)
        }
    }
}