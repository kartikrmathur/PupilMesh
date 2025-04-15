package com.example.pupilmesh.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pupilmesh.MainActivity
import com.example.pupilmesh.R
import com.example.pupilmesh.databinding.FragmentLoginBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: LoginViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        
        setupListeners()
        
        return root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observe auth state after view is created
        observeAuthState()
    }
    
    private fun setupListeners() {
        // Close button
        binding.buttonClose.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Google sign-in
        binding.buttonGoogle.setOnClickListener {
            // TODO: Implement Google Sign-in
            Toast.makeText(context, "Google sign-in not implemented", Toast.LENGTH_SHORT).show()
        }
        
        // Apple sign-in
        binding.buttonApple.setOnClickListener {
            // TODO: Implement Apple Sign-in
            Toast.makeText(context, "Apple sign-in not implemented", Toast.LENGTH_SHORT).show()
        }
        
        // Sign in button
        binding.buttonSignIn.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            
            if (validateInputs(email, password)) {
                viewModel.attemptLogin(email, password)
            }
        }
        
        // Forgot password
        binding.textForgotPassword.setOnClickListener {
            // TODO: Navigate to forgot password screen
            Toast.makeText(context, "Forgot password not implemented", Toast.LENGTH_SHORT).show()
        }
        
        // Sign up
        binding.textSignUp.setOnClickListener {
            // This is handled automatically in our implementation as new users are registered automatically
            Toast.makeText(context, "New users are registered automatically", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collectLatest { state ->
                if (!isAdded) return@collectLatest
                
                when (state) {
                    is AuthState.Loading -> {
                        showLoading(true)
                    }
                    is AuthState.Success -> {
                        showLoading(false)
                        
                        try {
                            // Navigate first, then try to show bottom navigation
                            findNavController().navigate(R.id.action_navigation_sign_in_to_navigation_main)
                            
                            // Safely get activity and show bottom navigation
                            val activity = activity
                            if (activity != null && activity is MainActivity) {
                                activity.showBottomNavigation(true)
                            }
                        } catch (e: Exception) {
                            Log.e("LoginFragment", "Navigation error: ${e.message}")
                        }
                    }
                    is AuthState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is AuthState.Idle -> {
                        showLoading(false)
                    }
                }
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        _binding?.let { binding ->
            binding.buttonSignIn.isEnabled = !isLoading
            // Show loading indicator if needed
        }
    }
    
    private fun showError(message: String) {
        if (isAdded) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }
        
        if (password.isEmpty() || password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }
        
        return isValid
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 