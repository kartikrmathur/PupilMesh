package com.example.pupilmesh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manga")
data class MangaEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val subTitle: String?,
    val status: String?,
    val thumb: String?,
    val summary: String?,
    val authors: List<String>?,
    val genres: List<String>?,
    val nsfw: Boolean?,
    val type: String?,
    val totalChapter: Int?,
    val createAt: Long?,
    val updateAt: Long?,
    // Additional fields for cache management
    val insertedAt: Long = System.currentTimeMillis(),
    val page: Int
) {
    // Convert to Manga domain model
    fun toManga(): Manga {
        return Manga(
            id = id,
            title = title,
            subTitle = subTitle,
            status = status,
            thumb = thumb,
            summary = summary,
            authors = authors,
            genres = genres,
            nsfw = nsfw,
            type = type,
            totalChapter = totalChapter,
            createAt = createAt,
            updateAt = updateAt
        )
    }

    companion object {
        // Convert from Manga domain model
        fun fromManga(manga: Manga, page: Int): MangaEntity {
            return MangaEntity(
                id = manga.id ?: "",
                title = manga.title,
                subTitle = manga.subTitle,
                status = manga.status,
                thumb = manga.thumb,
                summary = manga.summary,
                authors = manga.authors,
                genres = manga.genres,
                nsfw = manga.nsfw,
                type = manga.type,
                totalChapter = manga.totalChapter,
                createAt = manga.createAt,
                updateAt = manga.updateAt,
                page = page
            )
        }
    }
} 