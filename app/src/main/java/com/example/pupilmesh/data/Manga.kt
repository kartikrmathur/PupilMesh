package com.example.pupilmesh.data

import android.os.Parcel
import android.os.Parcelable
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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Long::class.java.classLoader) as? Long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(subTitle)
        parcel.writeString(status)
        parcel.writeString(thumb)
        parcel.writeString(summary)
        parcel.writeStringList(authors)
        parcel.writeStringList(genres)
        parcel.writeValue(nsfw)
        parcel.writeString(type)
        parcel.writeValue(totalChapter)
        parcel.writeValue(createAt)
        parcel.writeValue(updateAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Manga> {
        override fun createFromParcel(parcel: Parcel): Manga {
            return Manga(parcel)
        }

        override fun newArray(size: Int): Array<Manga?> {
            return arrayOfNulls(size)
        }
    }
} 