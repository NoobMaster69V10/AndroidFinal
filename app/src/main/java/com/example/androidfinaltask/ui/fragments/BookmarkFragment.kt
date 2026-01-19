package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidfinaltask.databinding.FragmentBookmarkBinding
import com.example.androidfinaltask.ui.adapter.NewsAdapter
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel

class BookmarkFragment : Fragment() {
    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()
    
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
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

        binding.rvBookmarks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@BookmarkFragment.adapter
        }

        viewModel.bookmarkedArticles.observe(viewLifecycleOwner) { articles ->
            if (articles.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvBookmarks.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvBookmarks.visibility = View.VISIBLE
                adapter.submitList(articles)
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterBookmarks(s.toString())
            }
        })
    }

    private fun filterBookmarks(query: String) {
        val allBookmarks = viewModel.bookmarkedArticles.value ?: emptyList()
        val filtered = if (query.isBlank()) {
            allBookmarks
        } else {
            allBookmarks.filter {
                it.title.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

