package com.example.androidfinaltask.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidfinaltask.data.model.Article
import com.example.androidfinaltask.databinding.ItemNewsBinding

class NewsAdapter(
    private val onItemClick: (Article) -> Unit
) : ListAdapter<Article, NewsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewsBinding.inflate(
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
        private val binding: ItemNewsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.apply {
                tvTitle.text = article.title
                tvCategory.text = article.category ?: "General"
                tvSource.text = article.source?.name ?: ""
                tvTime.text = formatTime(article.publishedAt)

                val imageUrl = article.imageUrl?.trim()
                if (imageUrl != null && imageUrl.isNotEmpty() && isValidImageUrl(imageUrl)) {
                    Glide.with(root.context)
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .fallback(android.R.drawable.ic_menu_gallery)
                        .into(ivNews)
                } else {
                    Glide.with(root.context)
                        .clear(ivNews)
                    ivNews.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                root.setOnClickListener {
                    onItemClick(article)
                }
            }
        }

        private fun isValidImageUrl(url: String): Boolean {
            return try {
                (url.startsWith("http://", ignoreCase = true) || 
                url.startsWith("https://", ignoreCase = true)) &&
                !url.equals("null", ignoreCase = true)
            } catch (e: Exception) {
                false
            }
        }

        private fun formatTime(time: String?): String {
            return time ?: "Just now"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}


