package com.example.androidfinaltask.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidfinaltask.data.model.Author
import com.example.androidfinaltask.databinding.ItemSourceBinding

class SourceAdapter(
    private val onFollowClick: (Author) -> Unit
) : ListAdapter<Author, SourceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSourceBinding.inflate(
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
        private val binding: ItemSourceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(author: Author) {
            binding.apply {
                tvSourceName.text = author.name
                tvFollowers.text = "${author.followers} Followers"
                btnFollow.text = if (author.isFollowing) "Following" else "+ Follow"
                
                if (author.logo != null) {
                    Glide.with(root.context)
                        .load(author.logo)
                        .centerCrop()
                        .into(ivLogo)
                }

                btnFollow.setOnClickListener {
                    onFollowClick(author)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Author>() {
        override fun areItemsTheSame(oldItem: Author, newItem: Author): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Author, newItem: Author): Boolean {
            return oldItem == newItem
        }
    }
}

