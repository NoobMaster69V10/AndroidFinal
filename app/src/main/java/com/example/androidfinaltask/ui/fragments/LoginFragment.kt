package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.androidfinaltask.databinding.FragmentLoginBinding
import com.example.androidfinaltask.ui.viewmodel.AuthViewModel
import com.example.androidfinaltask.ui.viewmodel.AuthState

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    var onLoginSuccess: (() -> Unit)? = null
    var onSignupClick: (() -> Unit)? = null
    var onForgotPasswordClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val rememberMe = binding.cbRemember.isChecked

            if (validateInputs(email, password)) {
                authViewModel.signIn(email, password, rememberMe)
            }
        }

        binding.tvSignup.setOnClickListener {
            if (onSignupClick != null) {
                onSignupClick?.invoke()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(com.example.androidfinaltask.R.id.fragment_container, SignupFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        observeAuthState()
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnLogin.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    if (onLoginSuccess != null) {
                        onLoginSuccess?.invoke()
                    } else {
                        parentFragmentManager.beginTransaction()
                            .replace(com.example.androidfinaltask.R.id.fragment_container, HomeFragment())
                            .commitAllowingStateLoss()
                    }
                }
                is AuthState.Error -> {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilUsername.error = "Valid email required"
            binding.tilUsername.isErrorEnabled = true
            binding.tvUsernameError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.tilUsername.error = null
            binding.tilUsername.isErrorEnabled = false
            binding.tvUsernameError.visibility = View.GONE
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

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

