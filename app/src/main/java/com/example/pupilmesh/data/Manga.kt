package com.example.pupilmesh.data

import com.google.gson.annotations.SerializedName

data class Manga(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("sub_title")
    val subTitle: String? = null,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("thumb")
    val thumb: String? = null,
    
    @SerializedName("summary")
    val summary: String? = null,
    
    @SerializedName("authors")
    val authors: List<String>? = null,
    
    @SerializedName("genres")
    val genres: List<String>? = null,
    
    @SerializedName("nsfw")
    val nsfw: Boolean? = null,
    
    @SerializedName("type")
    val type: String? = null,
    
    @SerializedName("total_chapter")
    val totalChapter: Int? = null,
    
    @SerializedName("create_at")
    val createAt: Long? = null,
    
    @SerializedName("update_at")
    val updateAt: Long? = null
) 