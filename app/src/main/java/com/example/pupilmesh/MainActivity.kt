package com.example.pupilmesh

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pupilmesh.databinding.ActivityMainBinding
import com.example.pupilmesh.util.SharedPreferencesManager
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefsManager = SharedPreferencesManager(this)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide bottom navigation initially
        binding.navView.visibility = View.GONE

        // Check authentication state asynchronously
        lifecycleScope.launch {
            val isSignedIn = withContext(Dispatchers.IO) {
                prefsManager.isSignedIn()
            }
            
            withContext(Dispatchers.Main) {
                handleNavigation(isSignedIn)
            }
        }
    }

    private fun handleNavigation(isSignedIn: Boolean) {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val navView: BottomNavigationView = binding.navView

        if (isSignedIn) {
            // User is signed in, navigate to main navigation
            navController.navigate(R.id.navigation_main)
            
            // Set up bottom navigation
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_manga,
                    R.id.navigation_face
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            navView.visibility = View.VISIBLE
        } else {
            // User is not signed in, hide bottom navigation
            navView.visibility = View.GONE
        }
    }

    fun showBottomNavigation(show: Boolean) {
        binding.navView.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}