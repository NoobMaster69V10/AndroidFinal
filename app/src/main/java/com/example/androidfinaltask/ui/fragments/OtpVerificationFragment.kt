package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidfinaltask.databinding.FragmentOtpVerificationBinding

class OtpVerificationFragment : Fragment() {
    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    var onBackClick: (() -> Unit)? = null
    var onVerifySuccess: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            onBackClick?.invoke()
        }

        setupOtpInputs()

        binding.btnVerify.setOnClickListener {
            val otp = getOtp()
            if (otp.length == 4) {
                if (otp == "4872") {
                    binding.tvError.visibility = View.GONE
                    onVerifySuccess?.invoke(otp)
                } else {
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupOtpInputs() {
        val inputs = listOf(binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4)
        
        inputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.isNotEmpty() == true && index < inputs.size - 1) {
                        inputs[index + 1].requestFocus()
                    }
                }
            })
        }
    }

    private fun getOtp(): String {
        return binding.etOtp1.text.toString() +
                binding.etOtp2.text.toString() +
                binding.etOtp3.text.toString() +
                binding.etOtp4.text.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

