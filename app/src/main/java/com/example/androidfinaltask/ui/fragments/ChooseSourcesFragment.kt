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
import com.example.androidfinaltask.databinding.FragmentChooseSourcesBinding
import com.example.androidfinaltask.ui.adapter.SourceAdapter
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel

class ChooseSourcesFragment : Fragment() {
    private var _binding: FragmentChooseSourcesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()
    
    private lateinit var sourceAdapter: SourceAdapter
    
    var onNextClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseSourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sourceAdapter = SourceAdapter { author ->
            viewModel.toggleAuthorFollow(author)
        }

        binding.rvSources.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = sourceAdapter
        }

        viewModel.authors.observe(viewLifecycleOwner) { authors ->
            sourceAdapter.submitList(authors)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterSources(s.toString())
            }
        })

        binding.btnNext.setOnClickListener {
            onNextClick?.invoke()
        }
    }

    private fun filterSources(query: String) {
        val allSources = viewModel.authors.value ?: emptyList()
        val filtered = if (query.isBlank()) {
            allSources
        } else {
            allSources.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        sourceAdapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

