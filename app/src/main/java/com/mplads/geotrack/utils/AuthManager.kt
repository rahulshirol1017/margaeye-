package com.mplads.geotrack.utils

import android.content.Context
import android.content.SharedPreferences

object AuthManager {

    private const val PREFS_NAME = "marga_eyes_auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_OFFICER_NAME = "officer_name"
    private const val KEY_WORK_ID = "work_id"
    private const val KEY_DESCRIPTION = "work_description"
    private const val KEY_AUTH_PROVIDER = "auth_provider"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveSession(
        context: Context,
        email: String,
        officerName: String,
        workId: String,
        description: String = "",
        provider: String = "GOOGLE"
    ) {
        getPrefs(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_EMAIL, email)
            .putString(KEY_OFFICER_NAME, officerName)
            .putString(KEY_WORK_ID, if (workId.isNotBlank()) workId else "WRK-2026")
            .putString(KEY_DESCRIPTION, description)
            .putString(KEY_AUTH_PROVIDER, provider)
            .apply()

        // Also sync with main prefs so CameraScreen picks it up seamlessly
        context.getSharedPreferences("marga_eyes_prefs", Context.MODE_PRIVATE).edit()
            .putString("worker_name", officerName)
            .putString("work_id", if (workId.isNotBlank()) workId else "WRK-2026")
            .putString("work_description", description)
            .apply()
    }

    fun getOfficerName(context: Context): String {
        return getPrefs(context).getString(KEY_OFFICER_NAME, "") ?: ""
    }

    fun getWorkId(context: Context): String {
        return getPrefs(context).getString(KEY_WORK_ID, "WRK-2026") ?: "WRK-2026"
    }

    fun getDescription(context: Context): String {
        return getPrefs(context).getString(KEY_DESCRIPTION, "") ?: ""
    }

    fun getUserEmail(context: Context): String {
        return getPrefs(context).getString(KEY_EMAIL, "") ?: ""
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
