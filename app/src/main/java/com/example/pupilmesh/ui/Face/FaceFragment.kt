package com.example.pupilmesh.ui.Face

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pupilmesh.databinding.FragmentFaceBinding
import com.example.pupilmesh.databinding.FragmentHomeBinding

class FaceFragment: Fragment() {

    private var _binding: FragmentFaceBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textFace
        textView.text = "Face Fragment"
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}