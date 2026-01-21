package com.example.androidfinaltask.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidfinaltask.data.model.Topic
import com.example.androidfinaltask.databinding.ItemTopicBinding

class TopicAdapter(
    private val onSaveClick: (Topic) -> Unit
) : ListAdapter<Topic, TopicAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopicBinding.inflate(
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
        private val binding: ItemTopicBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(topic: Topic) {
            binding.apply {
                tvTopicName.text = topic.name
                tvTopicDescription.text = topic.description ?: ""
                btnSave.text = if (topic.isSaved) "Saved" else "Save"
                
                btnSave.setOnClickListener {
                    onSaveClick(topic)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Topic>() {
        override fun areItemsTheSame(oldItem: Topic, newItem: Topic): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Topic, newItem: Topic): Boolean {
            return oldItem == newItem
        }
    }
}


