package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.androidfinaltask.databinding.FragmentSearchTopicsBinding
import com.example.androidfinaltask.ui.adapter.TopicAdapter
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel

class SearchTopicsFragment : Fragment() {
    private var _binding: FragmentSearchTopicsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()
    
    private lateinit var adapter: TopicAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchTopicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TopicAdapter { topic ->
            viewModel.toggleTopicSave(topic)
        }

        binding.rvTopics.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@SearchTopicsFragment.adapter
        }

        viewModel.topics.observe(viewLifecycleOwner) { topics ->
            adapter.submitList(topics)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

