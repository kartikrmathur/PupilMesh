package com.example.pupilmesh.ui.Manga

import androidx.navigation.NavDirections
import com.example.pupilmesh.data.Manga
import android.os.Bundle

class MangaFragmentDirections private constructor() {
    companion object {
        fun actionNavigationMangaToMangaDescriptionFragment(manga: Manga): NavDirections {
            return ActionWithManga(manga)
        }
    }

    private class ActionWithManga(private val manga: Manga) : NavDirections {
        override val actionId: Int = com.example.pupilmesh.R.id.action_navigation_manga_to_mangaDescriptionFragment

        override val arguments: Bundle
            get() {
                val bundle = Bundle()
                bundle.putParcelable(MangaDescriptionFragment.ARG_MANGA, manga)
                return bundle
            }
    }
} 