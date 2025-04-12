package com.example.pupilmesh.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pupilmesh.MainActivity
import com.example.pupilmesh.R
import com.example.pupilmesh.databinding.FragmentHomeBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var homeViewModel: HomeViewModel
    private var lastToastTime: Long = 0
    private val TOAST_COOLDOWN = 2000L // 2 seconds cooldown between toasts

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupViews()
        setupObservers()

        return root
    }

    private fun setupViews() {
        // Get references to the views
        val emailInput: TextInputEditText = binding.inputEmail
        val passwordInput: TextInputEditText = binding.inputPassword
        val signInButton: Button = binding.buttonSignIn
        
        // Set up the sign-in button click listener
        signInButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (validateInputs(email, password)) {
                lifecycleScope.launch {
                    homeViewModel.authenticate(email, password)
                }
            } else {
                showStatusMessage("Please enter valid email and password", false)
            }
        }

        // Add text change listeners for real-time validation
        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = emailInput.text.toString()
                if (email.isNotEmpty() && !email.contains("@")) {
                    showStatusMessage("Please enter a valid email address", false)
                }
            }
        }

        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = passwordInput.text.toString()
                if (password.isNotEmpty() && password.length < 6) {
                    showStatusMessage("Password must be at least 6 characters", false)
                }
            }
        }
    }

    private fun setupObservers() {
        // Observe authentication status changes
        lifecycleScope.launch {
            homeViewModel.isAuthenticated.collectLatest { isAuthenticated ->
                if (isAuthenticated) {
                    showStatusMessage("Sign in successful!", true)
                    
                    // Show bottom navigation and navigate to main navigation
                    (requireActivity() as MainActivity).showBottomNavigation()
                    findNavController().navigate(R.id.action_navigation_sign_in_to_navigation_main)
                }
            }
        }

        // Observe authentication messages
        lifecycleScope.launch {
            homeViewModel.authMessage.collectLatest { message ->
                if (!homeViewModel.isAuthenticated.value) {
                    showStatusMessage(message, false)
                }
            }
        }
    }

    private fun showStatusMessage(message: String, isSuccess: Boolean) {
        // Update status text view
        binding.textStatus.text = message
        binding.textStatus.setTextColor(
            resources.getColor(
                if (isSuccess) android.R.color.holo_green_dark else android.R.color.holo_red_dark,
                null
            )
        )

        // Show toast only for success messages and with cooldown
        if (isSuccess) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastToastTime >= TOAST_COOLDOWN) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                lastToastTime = currentTime
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        // Basic validation
        return email.isNotEmpty() && email.contains("@") && password.isNotEmpty() && password.length >= 6
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}