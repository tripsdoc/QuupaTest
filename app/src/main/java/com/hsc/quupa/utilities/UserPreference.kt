package com.hsc.quupa.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class UserPreference(context: Context) {
    companion object {
        const val IP_ADDRESS = "ipAddress"
        const val USER_PREF = "quupaPref"
    }

    private val userPref: SharedPreferences = context.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)

    @SuppressLint("CommitPrefEdits")
    fun setIP(ipAddress: String) {
        val editor: SharedPreferences.Editor = userPref.edit()
        editor.putString(IP_ADDRESS, ipAddress)
        editor.apply()
    }

    fun getIP(
        IP_ADDRESS: String,
        ipAddress: String = ""
    ): String? {
        return userPref.getString(IP_ADDRESS, ipAddress)
    }
}