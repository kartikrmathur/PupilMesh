package com.example.pupilmesh.repository

import android.util.Log
import com.example.pupilmesh.data.Manga
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

data class MangaResponse(
    val code: Int,
    val data: List<Manga>
)

class MangaRepository {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val TAG = "MangaRepository"

    suspend fun fetchManga(
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
                Log.d(TAG, "Response headers: ${response.headers}")
                
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e(TAG, "Error response body: $errorBody")
                    throw Exception("Failed to fetch manga: ${response.code}")
                }
                
                parseMangaResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
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