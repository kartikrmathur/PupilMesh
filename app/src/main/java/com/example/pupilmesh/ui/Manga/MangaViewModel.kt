package com.example.pupilmesh.ui.Manga

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pupilmesh.data.Manga
import com.example.pupilmesh.repository.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MangaViewModel(
    application: Application,
    private val repository: MangaRepository
) : AndroidViewModel(application) {
    private val _mangaList = MutableStateFlow<List<Manga>>(emptyList())
    val mangaList: StateFlow<List<Manga>> = _mangaList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore

    private var currentPage = 1
    private var isFetching = false
    private val TAG = "MangaViewModel"

    init {
        // Load initial cached data
        viewModelScope.launch {
            try {
                // Get max cached page to determine current page
                val maxCachedPage = repository.getMaxCachedPage()
                if (maxCachedPage > 0) {
                    currentPage = maxCachedPage + 1
                }
                
                // Observe all cached manga
                repository.getAllMangaFromCache().collect { cachedList ->
                    if (cachedList.isNotEmpty() && _mangaList.value.isEmpty()) {
                        _mangaList.value = cachedList
                        Log.d(TAG, "Loaded ${cachedList.size} manga from cache")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cached manga: ${e.message}", e)
            }
        }
        
        // Clean old cache
        viewModelScope.launch {
            try {
                repository.cleanOldCache()
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning old cache: ${e.message}", e)
            }
        }
    }

    fun fetchManga(apiKey: String, isRefresh: Boolean = false) {
        if (isFetching) return
        
        viewModelScope.launch {
            try {
                isFetching = true
                _isLoading.value = true
                _error.value = null

                if (isRefresh) {
                    currentPage = 1
                    _mangaList.value = emptyList()
                }

                Log.d(TAG, "Fetching page $currentPage")
                val response = repository.getManga(apiKey, currentPage, isRefresh)
                
                if (response.code == 200) {
                    val newMangaList = if (isRefresh) {
                        response.data
                    } else {
                        _mangaList.value + response.data
                    }
                    
                    _mangaList.value = newMangaList
                    _hasMore.value = response.data.isNotEmpty()
                    currentPage++
                } else {
                    _error.value = "Failed to fetch manga: ${response.code}"
                }
            } catch (e: Exception) {
                val errorMessage = "Error fetching manga: ${e.message}"
                Log.e(TAG, errorMessage, e)
                _error.value = errorMessage
            } finally {
                _isLoading.value = false
                isFetching = false
            }
        }
    }

    fun refresh(apiKey: String) {
        fetchManga(apiKey, true)
    }
} 