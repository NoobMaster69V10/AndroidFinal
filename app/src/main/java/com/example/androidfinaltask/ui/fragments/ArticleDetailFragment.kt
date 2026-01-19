package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
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

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.selectedArticle.observe(viewLifecycleOwner) { article ->
            article?.let { selected ->
                binding.tvTitle.text = selected.title
                binding.tvContent.text = selected.content ?: selected.description ?: ""
                binding.tvAuthor.text = selected.source?.name ?: selected.author ?: ""
                binding.tvLikes.text = "${selected.likes ?: 0}"
                binding.tvComments.text = "${selected.comments ?: 0}"

                if (selected.imageUrl != null) {
                    Glide.with(this)
                        .load(selected.imageUrl)
                        .centerCrop()
                        .into(binding.ivArticle)
                }

                val isBookmarked = viewModel.isBookmarked(selected)
                binding.ivBookmark.setImageResource(
                    if (isBookmarked) android.R.drawable.star_big_on
                    else android.R.drawable.star_big_off
                )

                binding.ivBookmark.setOnClickListener {
                    val currentlyBookmarked = viewModel.isBookmarked(selected)
                    if (currentlyBookmarked) {
                        viewModel.removeBookmark(selected)
                    } else {
                        viewModel.addBookmark(selected)
                    }
                    binding.ivBookmark.setImageResource(
                        if (viewModel.isBookmarked(selected)) android.R.drawable.star_big_on
                        else android.R.drawable.star_big_off
                    )
                }

                binding.ivComment.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(com.example.androidfinaltask.R.id.fragment_container, CommentFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

