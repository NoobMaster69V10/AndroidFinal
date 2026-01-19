package com.example.androidfinaltask.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidfinaltask.data.model.Comment
import com.example.androidfinaltask.databinding.ItemCommentBinding

class CommentAdapter : ListAdapter<Comment, CommentAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.apply {
                tvAuthorName.text = comment.authorName
                tvComment.text = comment.content
                
                val repliesCount = comment.replies?.size ?: 0
                if (repliesCount > 0) {
                    tvSeeMore.text = "See more ($repliesCount)"
                    tvSeeMore.visibility = View.VISIBLE
                } else {
                    tvSeeMore.visibility = View.GONE
                }

                if (comment.authorImage != null) {
                    Glide.with(root.context)
                        .load(comment.authorImage)
                        .circleCrop()
                        .into(ivProfile)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}

