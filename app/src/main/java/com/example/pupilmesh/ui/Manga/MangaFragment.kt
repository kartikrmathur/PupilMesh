package com.example.pupilmesh.ui.Manga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pupilmesh.databinding.FragmentMangaBinding

class MangaFragment: Fragment() {

    private var _binding: FragmentMangaBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangaBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textManga
        textView.text = "Manga Fragment"
        return root    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}