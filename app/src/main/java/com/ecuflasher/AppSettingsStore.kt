package com.ecuflasher

import android.content.Context

class AppSettingsStore(context: Context) {

    companion object {
        private const val PREFS_NAME = "ecuflasher_prefs"
        private const val KEY_DEVELOPER_MODE = "developer_mode"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDeveloperModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DEVELOPER_MODE, true)
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEVELOPER_MODE, enabled).apply()
    }
}
