package com.example.pupilmesh.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pupilmesh.data.AppDatabase
import com.example.pupilmesh.data.User
import com.example.pupilmesh.util.SecurityUtils
import com.example.pupilmesh.util.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val prefsManager = SharedPreferencesManager(application)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _authMessage = MutableStateFlow("")
    val authMessage: StateFlow<String> = _authMessage

    init {
        viewModelScope.launch {
            val isSignedIn = withContext(Dispatchers.IO) {
                prefsManager.isSignedIn()
            }
            _isAuthenticated.value = isSignedIn
        }
    }

    fun authenticate(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }

                if (user != null) {
                    val isValid = withContext(Dispatchers.Default) {
                        SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
                    }
                    
                    if (isValid) {
                        withContext(Dispatchers.IO) {
                            prefsManager.setSignedIn(email)
                        }
                        _isAuthenticated.value = true
                        _authMessage.value = "Successfully signed in"
                    } else {
                        _isAuthenticated.value = false
                        _authMessage.value = "Invalid password"
                    }
                } else {
                    // Create new user
                    val salt = withContext(Dispatchers.Default) {
                        SecurityUtils.generateSalt()
                    }
                    val passwordHash = withContext(Dispatchers.Default) {
                        SecurityUtils.hashPassword(password, salt)
                    }
                    val newUser = User(email, passwordHash, salt)
                    
                    withContext(Dispatchers.IO) {
                        userDao.insertUser(newUser)
                        prefsManager.setSignedIn(email)
                    }
                    
                    _isAuthenticated.value = true
                    _authMessage.value = "Account created successfully"
                }
            } catch (e: Exception) {
                _isAuthenticated.value = false
                _authMessage.value = "Authentication failed: ${e.message}"
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                prefsManager.clearSignedIn()
            }
            _isAuthenticated.value = false
        }
    }
}