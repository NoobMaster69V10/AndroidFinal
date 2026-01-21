package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.androidfinaltask.data.model.Article
import com.example.androidfinaltask.databinding.FragmentArticleDetailBinding
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel

class ArticleDetailFragment : Fragment() {
    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                android.util.Log.e("ArticleDetailFragment", "Error: $it")
                android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.selectedArticle.observe(viewLifecycleOwner) { article ->
            article?.let { selected ->
                binding.tvTitle.text = selected.title
                binding.tvContent.text = selected.content ?: selected.description ?: ""
                binding.tvAuthor.text = selected.source?.name ?: selected.author ?: ""
                binding.tvCategory.text = selected.category ?: "General"
                
                binding.tvTime.text = formatTime(selected.publishedAt)

                if (selected.imageUrl != null && selected.imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(selected.imageUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(binding.ivArticle)
                }
            }
        }

        viewModel.articleLikeCount.observe(viewLifecycleOwner) { count ->
            binding.tvLikes.text = formatCount(count)
        }

        viewModel.articleCommentCount.observe(viewLifecycleOwner) { count ->
            binding.tvComments.text = formatCount(count)
        }

        viewModel.isArticleLiked.observe(viewLifecycleOwner) { isLiked ->
            updateLikeIcon(isLiked)
        }

        viewModel.isArticleBookmarked.observe(viewLifecycleOwner) { isBookmarked ->
            updateBookmarkIcon(isBookmarked)
        }

        binding.ivLike.setOnClickListener {
            val article = viewModel.selectedArticle.value
            val articleId = article?.id
            if (!articleId.isNullOrEmpty()) {
                val isLoggedIn = com.example.androidfinaltask.data.repository.FirebaseRepository.isUserLoggedIn()
                if (!isLoggedIn) {
                    android.widget.Toast.makeText(
                        context,
                        "Please log in to like articles",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                viewModel.toggleLike(articleId)
            } else {
                android.util.Log.e("ArticleDetailFragment", "Article ID is null or empty")
            }
        }

        binding.ivBookmark.setOnClickListener {
            val article = viewModel.selectedArticle.value
            val articleId = article?.id
            if (!articleId.isNullOrEmpty()) {
                val isLoggedIn = com.example.androidfinaltask.data.repository.FirebaseRepository.isUserLoggedIn()
                if (!isLoggedIn) {
                    android.widget.Toast.makeText(
                        context,
                        "Please log in to bookmark articles",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                viewModel.toggleBookmark(articleId)
            } else {
                android.util.Log.e("ArticleDetailFragment", "Article ID is null or empty")
            }
        }

        binding.ivComment.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.androidfinaltask.R.id.fragment_container, CommentFragment())
                .addToBackStack(null)
                .commit()
        }
    }
    
    private fun formatTime(time: String?): String {
        if (time == null || time.isEmpty()) return "Just now"
        
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(time)
            
            if (date != null) {
                val now = System.currentTimeMillis()
                val diff = now - date.time
                val minutes = diff / (1000 * 60)
                val hours = diff / (1000 * 60 * 60)
                val days = diff / (1000 * 60 * 60 * 24)
                
                when {
                    minutes < 1 -> "Just now"
                    minutes < 60 -> "${minutes}m ago"
                    hours < 24 -> "${hours}h ago"
                    days < 7 -> "${days}d ago"
                    else -> {
                        val outputFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                        outputFormat.format(date)
                    }
                }
            } else {
                "Just now"
            }
        } catch (e: Exception) {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val date = inputFormat.parse(time)
                if (date != null) {
                    val now = System.currentTimeMillis()
                    val diff = now - date.time
                    val minutes = diff / (1000 * 60)
                    val hours = diff / (1000 * 60 * 60)
                    val days = diff / (1000 * 60 * 60 * 24)
                    
                    when {
                        minutes < 1 -> "Just now"
                        minutes < 60 -> "${minutes}m ago"
                        hours < 24 -> "${hours}h ago"
                        days < 7 -> "${days}d ago"
                        else -> "Just now"
                    }
                } else {
                    "Just now"
                }
            } catch (e2: Exception) {
                "Just now"
            }
        }
    }
    
    private fun formatCount(count: Int): String {
        return when {
            count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
            count >= 1000 -> String.format("%.1fK", count / 1000.0)
            else -> count.toString()
        }
    }
    
    private fun updateLikeIcon(isLiked: Boolean) {
        binding.ivLike.setImageResource(com.example.androidfinaltask.R.drawable.ic_heart)
        if (isLiked) {
            binding.ivLike.alpha = 1.0f
        } else {
            binding.ivLike.alpha = 0.4f
        }
    }
    
    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        binding.ivBookmark.setImageResource(com.example.androidfinaltask.R.drawable.ic_bookmark_detail)
        if (isBookmarked) {
            binding.ivBookmark.alpha = 1.0f
        } else {
            binding.ivBookmark.alpha = 0.4f // Darker/more transparent when not bookmarked
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

