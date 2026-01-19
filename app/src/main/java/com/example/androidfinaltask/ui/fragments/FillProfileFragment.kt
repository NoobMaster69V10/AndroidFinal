package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.androidfinaltask.databinding.FragmentFillProfileBinding
import com.example.androidfinaltask.ui.viewmodel.AuthViewModel

class FillProfileFragment : Fragment() {
    private var _binding: FragmentFillProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()
    
    var onNextClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFillProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = authViewModel.currentUser.value
        currentUser?.let { user ->
            binding.etUsername.setText(user.username)
            binding.etFullName.setText(user.fullName)
            binding.etEmail.setText(user.email)
            binding.etPhone.setText(user.phoneNumber)
        }

        binding.ivCamera.setOnClickListener {
        }

        binding.btnNext.setOnClickListener {
            saveProfileData()
            onNextClick?.invoke()
        }
    }

    private fun saveProfileData() {
        val currentUser = authViewModel.currentUser.value ?: return
        
        val updatedUser = currentUser.copy(
            username = binding.etUsername.text.toString(),
            fullName = binding.etFullName.text.toString(),
            email = binding.etEmail.text.toString(),
            phoneNumber = binding.etPhone.text.toString()
        )
        
        authViewModel.updateUser(updatedUser)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

