package com.mplads.geotrack.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

object AuthManager {

    private const val PREFS_NAME = "marga_eyes_auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_EXPIRES_AT = "auth_expires_at"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_OFFICER_NAME = "officer_name"
    private const val KEY_WORK_ID = "work_id"
    private const val KEY_DESCRIPTION = "work_description"
    private const val KEY_PROVIDER = "auth_provider"

    // 24 Hours in Milliseconds
    private const val SESSION_DURATION_MS = 24 * 60 * 60 * 1000L

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks whether the current session is valid and under 24 hours old.
     */
    fun isSessionValid(context: Context): Boolean {
        val prefs = getPrefs(context)
        val token = prefs.getString(KEY_TOKEN, null) ?: return false
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return token.isNotEmpty() && System.currentTimeMillis() < expiresAt
    }

    /**
     * Returns remaining session time in human readable format (e.g. "23h 45m remaining").
     */
    fun getRemainingSessionFormatted(context: Context): String {
        val prefs = getPrefs(context)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val remainingMs = expiresAt - System.currentTimeMillis()
        if (remainingMs <= 0) return "Session Expired"

        val hours = remainingMs / (1000 * 60 * 60)
        val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
        return "${hours}h ${minutes}m remaining"
    }

    /**
     * Sign in with Credentials & Password. Generates a 24-hour session.
     */
    fun loginWithCredentials(
        context: Context,
        email: String,
        password: String,
        officerName: String,
        workId: String,
        description: String
    ): Boolean {
        if (email.isBlank() || password.isBlank() || officerName.isBlank() || workId.isBlank()) {
            return false
        }

        val token = "SEC_TOKEN_" + UUID.randomUUID().toString().take(12)
        val expiresAt = System.currentTimeMillis() + SESSION_DURATION_MS

        getPrefs(context).edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putString(KEY_EMAIL, email)
            .putString(KEY_OFFICER_NAME, officerName)
            .putString(KEY_WORK_ID, workId)
            .putString(KEY_DESCRIPTION, description)
            .putString(KEY_PROVIDER, "CREDENTIALS")
            .apply()

        return true
    }

    /**
     * Sign in with Google Account. Generates a 24-hour session.
     */
    fun loginWithGoogle(
        context: Context,
        googleEmail: String,
        googleDisplayName: String,
        workId: String,
        description: String
    ): Boolean {
        val token = "GOOG_TOKEN_" + UUID.randomUUID().toString().take(12)
        val expiresAt = System.currentTimeMillis() + SESSION_DURATION_MS
        val officerName = if (googleDisplayName.isNotBlank()) googleDisplayName else googleEmail.substringBefore("@")

        getPrefs(context).edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putString(KEY_EMAIL, googleEmail)
            .putString(KEY_OFFICER_NAME, officerName)
            .putString(KEY_WORK_ID, if (workId.isNotBlank()) workId else "GOOG-PROJECT")
            .putString(KEY_DESCRIPTION, description)
            .putString(KEY_PROVIDER, "GOOGLE")
            .apply()

        return true
    }

    fun getOfficerName(context: Context): String {
        return getPrefs(context).getString(KEY_OFFICER_NAME, "") ?: ""
    }

    fun getWorkId(context: Context): String {
        return getPrefs(context).getString(KEY_WORK_ID, "") ?: ""
    }

    fun getDescription(context: Context): String {
        return getPrefs(context).getString(KEY_DESCRIPTION, "") ?: ""
    }

    fun getUserEmail(context: Context): String {
        return getPrefs(context).getString(KEY_EMAIL, "") ?: ""
    }

    fun getAuthProvider(context: Context): String {
        return getPrefs(context).getString(KEY_PROVIDER, "CREDENTIALS") ?: "CREDENTIALS"
    }

    /**
     * Terminate active session & logout.
     */
    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
