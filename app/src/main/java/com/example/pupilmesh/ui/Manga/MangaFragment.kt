package com.example.pupilmesh.ui.Manga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.pupilmesh.data.Manga
import com.example.pupilmesh.databinding.FragmentMangaBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MangaFragment : Fragment() {
    private var _binding: FragmentMangaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MangaViewModel
    private lateinit var adapter: MangaAdapter
    private val GRID_SPAN_COUNT = 3 // Changed from 2 to 3 columns to match the image

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel with factory
        viewModel = ViewModelProvider(
            this,
            MangaViewModelFactory(requireActivity().application)
        )[MangaViewModel::class.java]

        // Set up RecyclerView with GridLayoutManager
        val recyclerView: RecyclerView = binding.mangaRecyclerView
        val gridLayoutManager = GridLayoutManager(context, GRID_SPAN_COUNT)
        recyclerView.layoutManager = gridLayoutManager
        
        // Initialize adapter with click handler
        adapter = MangaAdapter { manga ->
            navigateToMangaDescription(manga)
        }
        
        recyclerView.adapter = adapter

        // Set up infinite scrolling with GridLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (!viewModel.isLoading.value && 
                    viewModel.hasMore.value && 
                    lastVisibleItemPosition >= totalItemCount - (GRID_SPAN_COUNT * 2)) {
                    // Load more when user is within 2 rows from the end
                    viewModel.fetchManga(requireContext().getString(com.example.pupilmesh.R.string.rapid_api_key))
                }
            }
        })

        // Set up pull-to-refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh(requireContext().getString(com.example.pupilmesh.R.string.rapid_api_key))
        }

        // Observe manga list
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mangaList.collectLatest { mangaList ->
                adapter.submitList(mangaList)
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.swipeRefreshLayout.isRefreshing = isLoading
            }
        }

        // Observe error state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    binding.errorText.text = it
                    binding.errorText.visibility = View.VISIBLE
                } ?: run {
                    binding.errorText.visibility = View.GONE
                }
            }
        }

        // Initial fetch
        viewModel.fetchManga(requireContext().getString(com.example.pupilmesh.R.string.rapid_api_key))
    }
    
    private fun navigateToMangaDescription(manga: Manga) {
        val action = MangaFragmentDirections.actionNavigationMangaToMangaDescriptionFragment(manga)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}