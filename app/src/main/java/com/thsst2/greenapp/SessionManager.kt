package com.thsst2.greenapp

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    fun saveLoginSession(uid: String?, email: String?) {
        sharedPref.edit().apply {
            putString("uid", uid)
            putString("email", email)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false)
    }

    fun clearSession() {
        sharedPref.edit { clear() }
    }

    fun getEmail(): String? {
        return sharedPref.getString("email", null)
    }

    fun getUid(): String? {
        return sharedPref.getString("uid", null)
    }
}