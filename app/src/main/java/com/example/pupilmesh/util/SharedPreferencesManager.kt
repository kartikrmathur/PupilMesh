package com.example.pupilmesh.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "pupil_mesh_prefs", Context.MODE_PRIVATE
    )

    fun setSignedIn(email: String) {
        sharedPreferences.edit().putString("signed_in_email", email).apply()
    }

    fun isSignedIn(): Boolean {
        return sharedPreferences.contains("signed_in_email")
    }

    fun getSignedInEmail(): String? {
        return sharedPreferences.getString("signed_in_email", null)
    }

    fun clearSignedIn() {
        sharedPreferences.edit().remove("signed_in_email").apply()
    }
} 