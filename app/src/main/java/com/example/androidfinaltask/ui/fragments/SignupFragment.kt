package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.androidfinaltask.databinding.FragmentSignupBinding
import com.example.androidfinaltask.ui.viewmodel.AuthViewModel
import com.example.androidfinaltask.ui.viewmodel.AuthState

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    var onSignupSuccess: (() -> Unit)? = null
    var onLoginClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignup.isEnabled = true
        
        observeAuthState()
        
        binding.btnSignup.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            Log.d("SignupFragment", "Signup button clicked")
            Log.d("SignupFragment", "Username: $username, Password length: ${password.length}, Confirm: ${confirmPassword.length}")

            if (validateInputs(username, password, confirmPassword)) {
                val email = if (android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                    username
                } else {
                    "$username@gmail.com"
                }
                Log.d("SignupFragment", "Starting signup with email: $email, username: $username")
                binding.btnSignup.isEnabled = false
                authViewModel.signUp(email, password, username)
            } else {
                Log.d("SignupFragment", "Validation failed")
                Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            if (onLoginClick != null) {
                onLoginClick?.invoke()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(com.example.androidfinaltask.R.id.fragment_container, LoginFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            Log.d("SignupFragment", "Auth state changed: $state")
            when (state) {
                is AuthState.Loading -> {
                    binding.btnSignup.isEnabled = false
                    Log.d("SignupFragment", "Loading state")
                }
                is AuthState.Success -> {
                    binding.btnSignup.isEnabled = true
                    Log.d("SignupFragment", "Success state - navigating to Home")
                    Toast.makeText(requireContext(), "Sign up successful!", Toast.LENGTH_SHORT).show()
                    if (onSignupSuccess != null) {
                        onSignupSuccess?.invoke()
                    } else {
                        parentFragmentManager.beginTransaction()
                            .replace(com.example.androidfinaltask.R.id.fragment_container, HomeFragment())
                            .commitAllowingStateLoss()
                    }
                }
                is AuthState.Error -> {
                    binding.btnSignup.isEnabled = true
                    Log.e("SignupFragment", "Error state: ${state.message}")
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                is AuthState.NotAuthenticated -> {
                    binding.btnSignup.isEnabled = true
                    Log.d("SignupFragment", "Not authenticated state")
                }
            }
        }
    }

    private fun validateInputs(username: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.error = "Username or email required"
            binding.tilUsername.isErrorEnabled = true
            try {
                binding.tvUsernameError.visibility = View.VISIBLE
            } catch (e: Exception) {
            }
            isValid = false
        } else {
            binding.tilUsername.error = null
            binding.tilUsername.isErrorEnabled = false
            try {
                binding.tvUsernameError.visibility = View.GONE
            } catch (e: Exception) {
            }
        }

        if (TextUtils.isEmpty(password) || password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            binding.tilPassword.isErrorEnabled = true
            binding.tvPasswordError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.tilPassword.error = null
            binding.tilPassword.isErrorEnabled = false
            binding.tvPasswordError.visibility = View.GONE
        }

        if (TextUtils.isEmpty(confirmPassword) || password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            binding.tilConfirmPassword.isErrorEnabled = true
            binding.tvConfirmPasswordError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
            binding.tilConfirmPassword.isErrorEnabled = false
            binding.tvConfirmPasswordError.visibility = View.GONE
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

