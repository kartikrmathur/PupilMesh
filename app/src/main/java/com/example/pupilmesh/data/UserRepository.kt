package com.example.pupilmesh.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.pupilmesh.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {
    
    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val sharedPreferences: SharedPreferences
    
    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    suspend fun registerUser(email: String, password: String): Long = withContext(Dispatchers.IO) {
        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)
        val user = User(email, passwordHash, salt)
        return@withContext userDao.insertUser(user)
    }
    
    suspend fun loginUser(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email) ?: return@withContext false
        return@withContext SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
    }
    
    suspend fun checkUserExists(email: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email)
        return@withContext user != null
    }
    
    fun setLoggedInUser(email: String) {
        sharedPreferences.edit().putString("logged_in_user_email", email).apply()
    }
    
    fun getLoggedInUser(): String? {
        return sharedPreferences.getString("logged_in_user_email", null)
    }
    
    fun clearLoggedInUser() {
        sharedPreferences.edit().remove("logged_in_user_email").apply()
    }
    
    fun isUserLoggedIn(): Boolean {
        return getLoggedInUser() != null
    }
} 