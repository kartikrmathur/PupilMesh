package com.example.pupilmesh.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.pupilmesh.data.AppDatabase
import com.example.pupilmesh.data.Manga
import com.example.pupilmesh.data.MangaEntity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

data class MangaResponse(
    val code: Int,
    val data: List<Manga>
)

class MangaRepository(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val TAG = "MangaRepository"
    private val mangaDao = AppDatabase.getDatabase(context).mangaDao()
    
    // Cache for 1 day
    private val CACHE_DURATION = TimeUnit.DAYS.toMillis(1)
    
    // Check network connectivity
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    // Get manga with caching
    suspend fun getManga(apiKey: String, page: Int, forceRefresh: Boolean): MangaResponse {
        // If force refresh or network available and cache is empty, fetch from API
        val shouldFetchFromNetwork = forceRefresh || (isNetworkAvailable() && (page == 1 || mangaDao.getMangaByPage(page).firstOrNull()?.isEmpty() != false))
        
        return if (shouldFetchFromNetwork) {
            try {
                val apiResponse = fetchMangaFromApi(apiKey, page)
                if (apiResponse.code == 200) {
                    // Cache the results
                    val mangaEntities = apiResponse.data.map { MangaEntity.fromManga(it, page) }
                    mangaDao.insertAll(mangaEntities)
                }
                apiResponse
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from API: ${e.message}", e)
                // If API call fails, try to return from cache
                val cachedManga = mangaDao.getMangaByPage(page).firstOrNull() ?: emptyList()
                MangaResponse(200, cachedManga.map { it.toManga() })
            }
        } else {
            // Return from cache
            Log.d(TAG, "Returning manga from cache for page $page")
            val cachedManga = mangaDao.getMangaByPage(page).first()
            MangaResponse(200, cachedManga.map { it.toManga() })
        }
    }
    
    // Get all manga from cache
    fun getAllMangaFromCache(): Flow<List<Manga>> {
        return mangaDao.getAllManga().map { entities ->
            entities.map { it.toManga() }
        }
    }
    
    // Clean old cache
    suspend fun cleanOldCache() {
        val timestamp = System.currentTimeMillis() - CACHE_DURATION
        mangaDao.deleteOlderThan(timestamp)
    }
    
    // Get max cached page
    suspend fun getMaxCachedPage(): Int {
        return mangaDao.getMaxPage() ?: 0
    }

    // Fetch from API
    private suspend fun fetchMangaFromApi(
        apiKey: String,
        page: Int = 1,
        genres: String? = "Harem,Fantasy",
        nsfw: Boolean = true,
        type: String = "all"
    ): MangaResponse {
        return withContext(Dispatchers.IO) {
            val url = "https://mangaverse-api.p.rapidapi.com/manga/fetch?page=$page&genres=$genres&nsfw=$nsfw&type=$type"
            Log.d(TAG, "Fetching manga from URL: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", "mangaverse-api.p.rapidapi.com")
                .build()

            // Log request details
            Log.d(TAG, "Request URL: ${request.url}")
            Log.d(TAG, "Request Headers: ${request.headers}")
            Log.d(TAG, "API Key length: ${apiKey.length}")
            Log.d(TAG, "API Key first 5 chars: ${apiKey.take(5)}...")

            try {
                val response = client.newCall(request).execute()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response message: ${response.message}")
                
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e(TAG, "Error response body: $errorBody")
                    when (response.code) {
                        401, 403 -> throw Exception("Authentication failed: API key may be invalid or expired (${response.code})")
                        429 -> throw Exception("Rate limit exceeded: Too many requests (${response.code})")
                        500, 502, 503, 504 -> throw Exception("Server error: The API service is experiencing issues (${response.code})")
                        else -> throw Exception("Failed to fetch manga: ${response.code} - ${response.message}")
                    }
                }
                
                parseMangaResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                if (e is java.net.UnknownHostException || e is java.net.SocketTimeoutException) {
                    throw Exception("Network error: Please check your internet connection")
                }
                throw e
            }
        }
    }

    private fun parseMangaResponse(response: Response): MangaResponse {
        try {
            val responseBody = response.body?.string() ?: throw Exception("Empty response body")
            Log.d(TAG, "Raw response body: $responseBody")
            
            // Parse the response as a JsonObject
            val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
            
            // Extract the code and data array
            val code = jsonObject.get("code").asInt
            val mangaArray = jsonObject.getAsJsonArray("data")
            
            // Parse the manga list
            val type = object : TypeToken<List<Manga>>() {}.type
            val mangaList = gson.fromJson<List<Manga>>(mangaArray, type)
            
            return MangaResponse(code, mangaList)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing manga response: ${e.message}", e)
            throw e
        }
    }
} 