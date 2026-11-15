package com.otakuzone.app.data.models

import com.google.gson.annotations.SerializedName

data class MangaVaultResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("took") val took: Double?,
    @SerializedName("data") val data: T?
)

data class MangaHomeData(
    @SerializedName("popular") val popular: List<MangaItem>?,
    @SerializedName("latest") val latest: List<MangaItem>?,
    @SerializedName("trending") val trending: List<MangaItem>?
)

data class MangaItem(
    @SerializedName("title") val title: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("cover") val cover: String?,
    @SerializedName("latest_chapter") val latestChapter: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("views") val views: String?
)

data class MangaDetails(
    @SerializedName("title") val title: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("cover") val cover: String?,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("authors") val authors: List<String>?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("chapters") val chapters: List<MangaChapter>?
)

data class MangaChapter(
    @SerializedName("title") val title: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("chapter_id") val chapterId: String?,
    @SerializedName("number") val number: String?,
    @SerializedName("uploaded") val uploaded: String?,
    @SerializedName("url") val url: String?
)

data class ChapterImageItem(
    @SerializedName("image") val image: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("index") val index: Int?
) {
    fun getImageUrl(): String {
        return image ?: url ?: ""
    }
}
