package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.androidfinaltask.databinding.FragmentChooseTopicsBinding
import com.example.androidfinaltask.ui.adapter.TopicAdapter
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel

class ChooseTopicsFragment : Fragment() {
    private var _binding: FragmentChooseTopicsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()
    
    private lateinit var topicAdapter: TopicAdapter
    
    var onNextClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseTopicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topicAdapter = TopicAdapter { topic ->
            viewModel.toggleTopicSave(topic)
        }

        binding.rvTopics.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = topicAdapter
        }

        viewModel.topics.observe(viewLifecycleOwner) { topics ->
            topicAdapter.submitList(topics)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterTopics(s.toString())
            }
        })

        binding.btnNext.setOnClickListener {
            onNextClick?.invoke()
        }
    }

    private fun filterTopics(query: String) {
        val allTopics = viewModel.topics.value ?: emptyList()
        val filtered = if (query.isBlank()) {
            allTopics
        } else {
            allTopics.filter {
                it.name.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
        topicAdapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


