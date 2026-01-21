package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidfinaltask.data.model.Comment
import com.example.androidfinaltask.data.repository.FirebaseRepository
import com.example.androidfinaltask.databinding.FragmentCommentBinding
import com.example.androidfinaltask.ui.adapter.CommentAdapter
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel
import java.util.UUID

class CommentFragment : Fragment() {
    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()
    
    private lateinit var adapter: CommentAdapter
    private var articleId: String? = null

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

        // Get article ID from selected article
        viewModel.selectedArticle.observe(viewLifecycleOwner) { article ->
            val newArticleId = article?.id
            if (newArticleId != null && newArticleId != articleId) {
                articleId = newArticleId
                viewModel.loadCommentsForArticle(articleId)
            }
        }

        // Load comments immediately if article is already selected
        val currentArticle = viewModel.selectedArticle.value
        if (currentArticle?.id != null) {
            articleId = currentArticle.id
            viewModel.loadCommentsForArticle(articleId)
        }

        // Observe comments
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            android.util.Log.d("CommentFragment", "Comments updated: ${comments.size} comments")
            adapter.submitList(comments)
        }

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.ivSend.setOnClickListener {
            val commentText = binding.etComment.text.toString().trim()
            if (commentText.isNotEmpty() && articleId != null) {
                val currentUser = FirebaseRepository.getCurrentUserId()
                if (currentUser == null) {
                    android.widget.Toast.makeText(
                        context,
                        "Please log in to comment",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                
                val userName = FirebaseRepository.getCurrentUserEmail()?.split("@")?.get(0) ?: "User"
                
                val newComment = Comment(
                    id = UUID.randomUUID().toString(),
                    authorName = userName,
                    authorImage = null,
                    content = commentText,
                    replies = null,
                    timestamp = System.currentTimeMillis().toString(),
                    articleId = articleId,
                    userId = currentUser
                )
                
                android.util.Log.d("CommentFragment", "Adding comment: articleId=$articleId, content=$commentText")
                viewModel.addComment(articleId, newComment)
                binding.etComment.text?.clear()
            } else if (articleId == null) {
                android.util.Log.e("CommentFragment", "Cannot add comment: articleId is null")
                android.widget.Toast.makeText(
                    context,
                    "Error: Article not found",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


