package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidfinaltask.databinding.FragmentSearchNewsBinding
import com.example.androidfinaltask.ui.adapter.NewsAdapter
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel

class SearchNewsFragment : Fragment() {
    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()
    
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NewsAdapter { article ->
            viewModel.selectArticle(article)
            parentFragmentManager.beginTransaction()
                .replace(com.example.androidfinaltask.R.id.fragment_container, ArticleDetailFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SearchNewsFragment.adapter
        }

        // Observe search results - this will be updated when user searches
        viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            adapter.submitList(searchResults)
        }

        // Observe search query to switch between search and latest news
        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (query.isNullOrEmpty()) {
                // When search is cleared, show latest news
                val latestNews = viewModel.latestNews.value ?: emptyList()
                adapter.submitList(latestNews)
            }
        }

        // Initial load - show latest news
        viewModel.latestNews.observe(viewLifecycleOwner) { news ->
            val query = viewModel.searchQuery.value
            if (query.isNullOrEmpty()) {
                adapter.submitList(news)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


