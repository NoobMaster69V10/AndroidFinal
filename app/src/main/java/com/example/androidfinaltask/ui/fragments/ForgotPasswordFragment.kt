package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.androidfinaltask.databinding.FragmentForgotPasswordBinding
import com.example.androidfinaltask.ui.viewmodel.AuthViewModel
import com.example.androidfinaltask.ui.viewmodel.AuthState

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    var onBackClick: (() -> Unit)? = null
    var onSubmitClick: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            onBackClick?.invoke()
        }

        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (TextUtils.isEmpty(email)) {
                binding.tilEmail.error = "Email required"
                binding.tilEmail.isErrorEnabled = true
            } else {
                binding.tilEmail.error = null
                binding.tilEmail.isErrorEnabled = false
                authViewModel.sendPasswordResetEmail(email)
            }
        }

        observeAuthState()
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnSubmit.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(requireContext(), "Password reset email sent!", Toast.LENGTH_SHORT).show()
                    onBackClick?.invoke()
                }
                is AuthState.Error -> {
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

