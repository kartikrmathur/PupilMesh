package com.example.pupilmesh.ui.Manga

import androidx.navigation.NavDirections
import android.os.Bundle

class MangaDescriptionFragmentDirections private constructor() {
    companion object {
        fun actionMangaDescriptionFragmentToFaceFragment(): NavDirections {
            return ActionToFaceFragment()
        }
    }

    private class ActionToFaceFragment : NavDirections {
        override val actionId: Int = com.example.pupilmesh.R.id.action_mangaDescriptionFragment_to_navigation_face
        override val arguments: Bundle = Bundle()
    }
} 