package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidfinaltask.databinding.FragmentResetPasswordBinding

class ResetPasswordFragment : Fragment() {
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    var onBackClick: (() -> Unit)? = null
    var onSubmitSuccess: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            onBackClick?.invoke()
        }

        binding.btnSubmit.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validatePasswords(newPassword, confirmPassword)) {
                onSubmitSuccess?.invoke()
            }
        }
    }

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        if (TextUtils.isEmpty(newPassword)) {
            binding.tilNewPassword.error = "Password required"
            binding.tilNewPassword.isErrorEnabled = true
            return false
        }

        if (newPassword.length < 6) {
            binding.tilNewPassword.error = "Password must be at least 6 characters"
            binding.tilNewPassword.isErrorEnabled = true
            return false
        }

        if (newPassword != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            binding.tilConfirmPassword.isErrorEnabled = true
            return false
        }

        binding.tilNewPassword.error = null
        binding.tilNewPassword.isErrorEnabled = false
        binding.tilConfirmPassword.error = null
        binding.tilConfirmPassword.isErrorEnabled = false
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

