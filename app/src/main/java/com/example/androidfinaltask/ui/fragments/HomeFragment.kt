package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.androidfinaltask.databinding.FragmentHomeBinding
import com.example.androidfinaltask.ui.adapter.NewsAdapter
import com.example.androidfinaltask.ui.adapter.TrendingNewsAdapter
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()
    
    private lateinit var trendingAdapter: TrendingNewsAdapter
    private lateinit var latestAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupSearchView()
        setupObservers()
    }

    private fun setupRecyclerViews() {
        trendingAdapter = TrendingNewsAdapter { article ->
            viewModel.selectArticle(article)
            navigateToDetail()
        }

        binding.rvTrending.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
        }

        latestAdapter = NewsAdapter { article ->
            viewModel.selectArticle(article)
            navigateToDetail()
        }

        binding.rvLatest.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = latestAdapter
        }
    }

    private fun setupSearchView() {
        val adapter = SearchPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "News"
                1 -> "Topics"
                2 -> "Author"
                else -> ""
            }
        }.attach()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    viewModel.searchArticles(query)
                    showSearchResults()
                } else {
                    viewModel.searchArticles("")
                    showHomeContent()
                }
            }
        })
    }

    private fun showSearchResults() {
        binding.svContent.visibility = View.GONE
        binding.llSearchResults.visibility = View.VISIBLE
    }

    private fun showHomeContent() {
        binding.svContent.visibility = View.VISIBLE
        binding.llSearchResults.visibility = View.GONE
    }

    private fun setupObservers() {
        viewModel.trendingNews.observe(viewLifecycleOwner) { news ->
            trendingAdapter.submitList(news)
        }

        viewModel.latestNews.observe(viewLifecycleOwner) { news ->
            latestAdapter.submitList(news)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun navigateToDetail() {
        parentFragmentManager.beginTransaction()
            .replace(com.example.androidfinaltask.R.id.fragment_container, ArticleDetailFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SearchPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SearchNewsFragment()
                1 -> SearchTopicsFragment()
                2 -> SearchAuthorFragment()
                else -> SearchNewsFragment()
            }
        }
    }
}

