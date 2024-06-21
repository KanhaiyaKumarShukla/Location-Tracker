package com.exa.android.loctrace.Helper

import android.content.Context
import android.content.SharedPreferences

object PreferencesHelper {
 // to set the switch when location is on and app is closed then again whenever app is open the switch remains open
    private const val PREFS_NAME = "location_prefs"
    private const val KEY_IS_LOCATION_SERVICE_RUNNING = "is_location_service_running"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setLocationServiceRunning(context: Context, isRunning: Boolean) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOCATION_SERVICE_RUNNING, isRunning)
        editor.apply()
    }

    fun isLocationServiceRunning(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOCATION_SERVICE_RUNNING, false)
    }
}
