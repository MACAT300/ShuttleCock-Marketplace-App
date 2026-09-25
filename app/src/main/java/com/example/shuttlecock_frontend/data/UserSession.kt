package com.example.shuttlecock_frontend.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Holds the logged-in user's info + JWT token.
 * Backed by SharedPreferences so the session survives app restarts.
 * Call UserSession.init(context) once, e.g. in the first Activity's onCreate.
 */
object UserSession {

    private const val PREFS_NAME = "shuttlecock_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "userId"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_CART_ID = "cartId"

    private const val KEY_USER_EMAIL = "userEmail"

    private const val KEY_IS_GOOGLE = "isGoogleAccount"
    private const val KEY_AVATAR_URL = "avatarUrl"

    private lateinit var prefs: SharedPreferences
    private var initialized = false


    fun init(context: Context) {
        if (initialized) return
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        initialized = true
    }

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var userId: Int
        get() = prefs.getInt(KEY_USER_ID, -1)
        set(value) = prefs.edit().putInt(KEY_USER_ID, value).apply()

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var avatarUrl: String?
        get() = prefs.getString(KEY_AVATAR_URL, null)
        set(value) = prefs.edit().putString(KEY_AVATAR_URL, value).apply()

    var isGoogleAccount: Boolean
        get() = prefs.getBoolean(KEY_IS_GOOGLE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_GOOGLE, value).apply()

    // Cached cart id for the current user so we don't re-create/re-fetch it every screen.
    var cartId: Int
        get() = prefs.getInt(KEY_CART_ID, -1)
        set(value) = prefs.edit().putInt(KEY_CART_ID, value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank() && userId != -1

    fun clear() {
        prefs.edit().clear().apply()
    }
}
