package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.androidfinaltask.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    var pageIndex: Int = 0
    var onNextClick: (() -> Unit)? = null
    var onBackClick: (() -> Unit)? = null
    var onGetStartedClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnNext.setOnClickListener {
            if (pageIndex == 2) {
                onGetStartedClick?.invoke()
            } else {
                onNextClick?.invoke()
            }
        }

        binding.btnBack.setOnClickListener {
            onBackClick?.invoke()
        }

        if (pageIndex == 0) {
            binding.btnBack.visibility = View.GONE
        } else {
            binding.btnBack.visibility = View.VISIBLE
        }

        if (pageIndex == 2) {
            binding.btnNext.text = "Get Started"
        } else {
            binding.btnNext.text = "Next"
        }

        setupIndicators()
        loadImage()
    }

    private fun setupIndicators() {
        binding.indicators.removeAllViews()
        for (i in 0..2) {
            val indicator = View(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    if (i == pageIndex) 24 else 8,
                    8
                ).apply {
                    setMargins(4, 0, 4, 0)
                }
                setBackgroundResource(android.R.drawable.presence_invisible)
                background.setAlpha(if (i == pageIndex) 255 else 128)
                setBackgroundColor(if (i == pageIndex) resources.getColor(com.example.androidfinaltask.R.color.primary, null) else resources.getColor(com.example.androidfinaltask.R.color.divider, null))
            }
            binding.indicators.addView(indicator)
        }
    }

    private fun loadImage() {
        val imageResIds: List<Int?> = listOf(
            null,
            null,
            null
        )
        
        val imageResId = imageResIds.getOrNull(pageIndex)
        
        when (pageIndex) {
            0 -> {
            }
            1 -> {
            }
            2 -> {
            }
        }
        
        if (imageResId != null) {
            Glide.with(this)
                .load(imageResId)
                .centerCrop()
                .into(binding.ivOnboarding)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(index: Int): OnboardingFragment {
            return OnboardingFragment().apply {
                pageIndex = index
            }
        }
    }
}

