package com.example.pupilmesh.ui.Manga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pupilmesh.data.Manga
import com.example.pupilmesh.databinding.FragmentMangaDescriptionBinding

class MangaDescriptionFragment : Fragment() {
    private var _binding: FragmentMangaDescriptionBinding? = null
    private val binding get() = _binding!!
    private var manga: Manga? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            manga = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                args.getParcelable(ARG_MANGA, Manga::class.java)
            } else {
                @Suppress("DEPRECATION")
                args.getParcelable(ARG_MANGA)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangaDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up navigation for the bottom tabs
        binding.mangaButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.faceButton.setOnClickListener {
            // Navigate to Face fragment
            // This may need adjustment based on your navigation graph
            findNavController().navigate(MangaDescriptionFragmentDirections.actionMangaDescriptionFragmentToFaceFragment())
        }

        // Display manga details
        manga?.let { manga ->
            // Load cover image
            manga.thumb?.let { thumbUrl ->
                Glide.with(this)
                    .load(thumbUrl)
                    .into(binding.mangaCover)
            }

            // Set text fields
            binding.mangaTitle.text = manga.title
            binding.mangaSubtitle.text = buildRepeatedSubtitle(manga)
            binding.mangaSummary.text = manga.summary ?: "No summary available"
            
            // Set up the favorite button
            binding.favoriteButton.setOnClickListener {
                // TODO: Add to favorites functionality
            }
        }
    }
    
    private fun buildRepeatedSubtitle(manga: Manga): String {
        val subtitle = manga.subTitle ?: ""
        val koreanTitle = manga.subTitle?.let { "• ${it}" } ?: ""
        return "${manga.title} • ${manga.title} ${koreanTitle}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_MANGA = "manga"
    }
} 