package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.androidfinaltask.databinding.FragmentAuthorProfileBinding
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel
import com.google.android.material.tabs.TabLayoutMediator

class AuthorProfileFragment : Fragment() {
    private var _binding: FragmentAuthorProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AuthorPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "News"
                1 -> "Recent"
                else -> ""
            }
        }.attach()

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val author = viewModel.authors.value?.firstOrNull()
        author?.let {
            binding.tvAuthorName.text = it.name
            if (it.logo != null) {
                Glide.with(this)
                    .load(it.logo)
                    .centerCrop()
                    .into(binding.ivLogo)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class AuthorPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SearchNewsFragment()
                1 -> SearchNewsFragment()
                else -> SearchNewsFragment()
            }
        }
    }
}

