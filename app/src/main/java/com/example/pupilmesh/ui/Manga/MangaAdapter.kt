package com.example.pupilmesh.ui.Manga

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pupilmesh.data.Manga
import com.example.pupilmesh.databinding.ItemMangaBinding

class MangaAdapter(
    private val onItemClick: (Manga) -> Unit
) : ListAdapter<Manga, MangaAdapter.MangaViewHolder>(MangaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
        val binding = ItemMangaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MangaViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MangaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MangaViewHolder(
        private val binding: ItemMangaBinding,
        private val onItemClick: (Manga) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(manga: Manga) {
            binding.titleText.text = manga.title
            binding.subTitleText.text = manga.subTitle ?: ""
            binding.summaryText.text = manga.summary
            binding.statusText.text = manga.status
            binding.typeText.text = manga.type
            binding.chaptersText.text = "Chapters: ${manga.totalChapter ?: 0}"
            
            // Load cover image using Glide
            manga.thumb?.let { thumbUrl ->
                Glide.with(binding.root.context)
                    .load(thumbUrl)
                    .into(binding.coverImage)
            }

            // Set click listener on the root view
            binding.root.setOnClickListener {
                onItemClick(manga)
            }
        }
    }

    private class MangaDiffCallback : DiffUtil.ItemCallback<Manga>() {
        override fun areItemsTheSame(oldItem: Manga, newItem: Manga): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Manga, newItem: Manga): Boolean {
            return oldItem == newItem
        }
    }
} 