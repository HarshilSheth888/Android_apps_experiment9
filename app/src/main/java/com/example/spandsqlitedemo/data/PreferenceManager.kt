package com.example.spandsqlitedemo.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREF_NAME = "task_pulse_prefs"

        const val KEY_USER_NAME = "user_name"
        const val KEY_THEME_ACCENT = "theme_accent" // "violet", "coral", "emerald", "amber"
        const val KEY_COMPACT_VIEW = "compact_view"
        const val KEY_AUTO_SORT_COMPLETED = "auto_sort_completed"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_DEFAULT_CATEGORY = "default_category"
        const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
    }

    var userName: String
        get() = sharedPreferences.getString(KEY_USER_NAME, "Alex") ?: "Alex"
        set(value) {
            sharedPreferences.edit().putString(KEY_USER_NAME, value).apply()
            updateSyncTime()
        }

    var themeAccent: String
        get() = sharedPreferences.getString(KEY_THEME_ACCENT, "violet") ?: "violet"
        set(value) {
            sharedPreferences.edit().putString(KEY_THEME_ACCENT, value).apply()
            updateSyncTime()
        }

    var isCompactView: Boolean
        get() = sharedPreferences.getBoolean(KEY_COMPACT_VIEW, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_COMPACT_VIEW, value).apply()
            updateSyncTime()
        }

    var isAutoSortCompleted: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_SORT_COMPLETED, true)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_AUTO_SORT_COMPLETED, value).apply()
            updateSyncTime()
        }

    var isNotificationsEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
            updateSyncTime()
        }

    var defaultCategory: String
        get() = sharedPreferences.getString(KEY_DEFAULT_CATEGORY, "Work") ?: "Work"
        set(value) {
            sharedPreferences.edit().putString(KEY_DEFAULT_CATEGORY, value).apply()
            updateSyncTime()
        }

    val lastSyncTimestamp: Long
        get() = sharedPreferences.getLong(KEY_LAST_SYNC_TIMESTAMP, System.currentTimeMillis())

    private fun updateSyncTime() {
        sharedPreferences.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    fun getAllPreferences(): Map<String, *> {
        return sharedPreferences.all
    }

    fun resetToDefaults() {
        sharedPreferences.edit().clear().apply()
        // Re-initialize with crisp defaults
        userName = "Alex"
        themeAccent = "violet"
        isCompactView = false
        isAutoSortCompleted = true
        isNotificationsEnabled = true
        defaultCategory = "Work"
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
