package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidfinaltask.databinding.FragmentSearchAuthorBinding
import com.example.androidfinaltask.ui.adapter.SourceAdapter
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel

class SearchAuthorFragment : Fragment() {
    private var _binding: FragmentSearchAuthorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()
    
    private lateinit var adapter: SourceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAuthorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SourceAdapter { author ->
            viewModel.toggleAuthorFollow(author)
        }

        binding.rvAuthors.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SearchAuthorFragment.adapter
        }

        viewModel.authors.observe(viewLifecycleOwner) { authors ->
            adapter.submitList(authors)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

