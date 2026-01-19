package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.androidfinaltask.databinding.FragmentEditProfileBinding
import com.example.androidfinaltask.ui.viewmodel.AuthViewModel
import com.example.androidfinaltask.ui.viewmodel.AuthState

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.etUsername.setText(it.username)
                binding.etFullName.setText(it.fullName)
                binding.etEmail.setText(it.email)
                binding.etPhone.setText(it.phoneNumber)
                binding.etBio.setText(it.bio)
                binding.etWebsite.setText(it.website)
                
                if (it.profileImageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(it.profileImageUrl)
                        .circleCrop()
                        .into(binding.ivProfile)
                }
            }
        }

        binding.ivClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.ivSave.setOnClickListener {
            saveProfile()
        }

        observeAuthState()
    }

    private fun saveProfile() {
        val currentUser = authViewModel.currentUser.value ?: return
        
        val updatedUser = currentUser.copy(
            username = binding.etUsername.text.toString(),
            fullName = binding.etFullName.text.toString(),
            email = binding.etEmail.text.toString(),
            phoneNumber = binding.etPhone.text.toString(),
            bio = binding.etBio.text.toString(),
            website = binding.etWebsite.text.toString()
        )
        
        authViewModel.updateUser(updatedUser)
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.ivSave.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.ivSave.isEnabled = true
                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                is AuthState.Error -> {
                    binding.ivSave.isEnabled = true
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

