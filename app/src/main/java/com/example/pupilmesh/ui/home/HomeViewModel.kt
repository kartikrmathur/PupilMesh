package com.example.pupilmesh.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pupilmesh.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _authMessage = MutableStateFlow("")
    val authMessage: StateFlow<String> = _authMessage

    init {
        viewModelScope.launch {
            _isAuthenticated.value = userRepository.isUserLoggedIn()
        }
    }

    fun authenticate(email: String, password: String) {
        viewModelScope.launch {
            try {
                val userExists = userRepository.checkUserExists(email)

                if (userExists) {
                    // Try to login
                    val loginSuccess = userRepository.loginUser(email, password)
                    if (loginSuccess) {
                        userRepository.setLoggedInUser(email)
                        _isAuthenticated.value = true
                        _authMessage.value = "Successfully signed in"
                    } else {
                        _isAuthenticated.value = false
                        _authMessage.value = "Invalid password"
                    }
                } else {
                    // Create new user
                    userRepository.registerUser(email, password)
                    userRepository.setLoggedInUser(email)
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
            userRepository.clearLoggedInUser()
            _isAuthenticated.value = false
        }
    }
}