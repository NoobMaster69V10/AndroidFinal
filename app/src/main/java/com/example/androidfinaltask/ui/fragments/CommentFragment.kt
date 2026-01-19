package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidfinaltask.data.model.Comment
import com.example.androidfinaltask.databinding.FragmentCommentBinding
import com.example.androidfinaltask.ui.adapter.CommentAdapter
import java.util.UUID

class CommentFragment : Fragment() {
    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: CommentAdapter

    private val comments = mutableListOf(
        Comment(UUID.randomUUID().toString(), "Wilson Frank", null, "Lorem ipsum is simply dummy text of the printing and typesetting industry.", listOf(), null),
        Comment(UUID.randomUUID().toString(), "Madelyn Bator", null, "Lorem ipsum is simply dummy text of the printing and typesetting industry.", listOf(), null),
        Comment(UUID.randomUUID().toString(), "Monica Watson", null, "Lorem ipsum is simply dummy text of the printing and typesetting industry.", listOf(), null)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CommentAdapter()

        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CommentFragment.adapter
        }

        adapter.submitList(comments)

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.ivSend.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            if (!TextUtils.isEmpty(commentText)) {
                val newComment = Comment(
                    UUID.randomUUID().toString(),
                    "You",
                    null,
                    commentText,
                    listOf(),
                    null
                )
                comments.add(0, newComment)
                adapter.submitList(comments.toList())
                binding.etComment.text?.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

