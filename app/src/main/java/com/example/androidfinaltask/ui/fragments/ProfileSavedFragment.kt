package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.androidfinaltask.databinding.FragmentProfilePostsBinding
import com.example.androidfinaltask.ui.adapter.NewsAdapter
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel

class ProfileSavedFragment : Fragment() {
    private var _binding: FragmentProfilePostsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()
    
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePostsBinding.inflate(inflater, container, false)
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

        binding.rvPosts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ProfileSavedFragment.adapter
        }

        viewModel.bookmarkedArticles.observe(viewLifecycleOwner) { articles ->
            adapter.submitList(articles)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

