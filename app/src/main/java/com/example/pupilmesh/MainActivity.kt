package com.example.pupilmesh

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pupilmesh.data.UserRepository
import com.example.pupilmesh.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository
    private var initialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            _binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            userRepository = UserRepository(this)
            initialized = true

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            binding.navView.setupWithNavController(navController)

            // Set destination changed listener to manage bottom navigation visibility
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.navigation_sign_in -> showBottomNavigation(false)
                    else -> {
                        if (userRepository.isUserLoggedIn()) {
                            showBottomNavigation(true)
                        }
                    }
                }
            }

            // Check if user is logged in
            checkUserLogin()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}")
        }
    }

    fun showBottomNavigation(show: Boolean) {
        try {
            if (!initialized || _binding == null) {
                Log.d("MainActivity", "Binding not initialized yet, skipping navigation change")
                return
            }
            binding.navView.visibility = if (show) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            Log.e("MainActivity", "Error showing bottom navigation: ${e.message}")
        }
    }

    private fun checkUserLogin() {
        if (userRepository.isUserLoggedIn()) {
            // User is already logged in, navigate to main
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navController.navigate(R.id.navigation_main)
            showBottomNavigation(true)
        } else {
            // User is not logged in, stay at sign in screen
            showBottomNavigation(false)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        initialized = false
    }
}