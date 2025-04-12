package com.example.pupilmesh.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "PupilMeshPrefs",
        Context.MODE_PRIVATE
    )

    fun setSignedIn(email: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SIGNED_IN_EMAIL, email)
            putBoolean(KEY_IS_SIGNED_IN, true)
            apply()
        }
    }

    fun clearSignedIn() {
        sharedPreferences.edit().apply {
            remove(KEY_SIGNED_IN_EMAIL)
            putBoolean(KEY_IS_SIGNED_IN, false)
            apply()
        }
    }

    fun isSignedIn(): Boolean = sharedPreferences.getBoolean(KEY_IS_SIGNED_IN, false)
    fun getSignedInEmail(): String? = sharedPreferences.getString(KEY_SIGNED_IN_EMAIL, null)

    companion object {
        private const val KEY_IS_SIGNED_IN = "is_signed_in"
        private const val KEY_SIGNED_IN_EMAIL = "signed_in_email"
    }
} 