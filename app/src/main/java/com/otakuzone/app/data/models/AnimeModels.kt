package com.otakuzone.app.data.models

import com.google.gson.annotations.SerializedName

data class AniListResponse<T>(
    @SerializedName("data") val data: T?,
    @SerializedName("errors") val errors: List<AniListError>?
)

data class AniListError(
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: Int?
)

data class AniListMediaData(
    @SerializedName("Media") val media: AniListMedia?,
    @SerializedName("Page") val page: AniListPage?
)

data class AniListPage(
    @SerializedName("pageInfo") val pageInfo: PageInfo?,
    @SerializedName("media") val media: List<AniListMedia>?,
    @SerializedName("airingSchedules") val airingSchedules: List<AiringScheduleEntry>?
)

data class PageInfo(
    @SerializedName("total") val total: Int?,
    @SerializedName("currentPage") val currentPage: Int?,
    @SerializedName("lastPage") val lastPage: Int?,
    @SerializedName("hasNextPage") val hasNextPage: Boolean?
)

data class AniListMedia(
    @SerializedName("id") val id: Int,
    @SerializedName("idMal") val idMal: Int?,
    @SerializedName("title") val title: MediaTitle?,
    @SerializedName("description") val description: String?,
    @SerializedName("format") val format: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("episodes") val episodes: Int?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("bannerImage") val bannerImage: String?,
    @SerializedName("coverImage") val coverImage: MediaCoverImage?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("averageScore") val averageScore: Int?,
    @SerializedName("popularity") val popularity: Int?,
    @SerializedName("nextAiringEpisode") val nextAiringEpisode: NextAiringEpisode?,
    @SerializedName("mediaListEntry") val mediaListEntry: MediaListEntry?
) {
    fun getDisplayName(): String {
        return title?.english?.takeIf { it.isNotBlank() }
            ?: title?.romaji?.takeIf { it.isNotBlank() }
            ?: title?.native
            ?: "Unknown Anime"
    }

    fun getBestCover(): String {
        return coverImage?.extraLarge
            ?: coverImage?.large
            ?: coverImage?.medium
            ?: bannerImage
            ?: ""
    }
}

data class MediaTitle(
    @SerializedName("romaji") val romaji: String?,
    @SerializedName("english") val english: String?,
    @SerializedName("native") val native: String?
)

data class MediaCoverImage(
    @SerializedName("extraLarge") val extraLarge: String?,
    @SerializedName("large") val large: String?,
    @SerializedName("medium") val medium: String?,
    @SerializedName("color") val color: String?
)

data class NextAiringEpisode(
    @SerializedName("id") val id: Int,
    @SerializedName("airingAt") val airingAt: Long,
    @SerializedName("timeUntilAiring") val timeUntilAiring: Long,
    @SerializedName("episode") val episode: Int
)

data class AiringScheduleEntry(
    @SerializedName("id") val id: Int,
    @SerializedName("airingAt") val airingAt: Long,
    @SerializedName("episode") val episode: Int,
    @SerializedName("media") val media: AniListMedia?
)

data class MediaListCollectionData(
    @SerializedName("MediaListCollection") val mediaListCollection: MediaListCollection?
)

data class MediaListCollection(
    @SerializedName("lists") val lists: List<MediaListGroup>?
)

data class MediaListGroup(
    @SerializedName("name") val name: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("entries") val entries: List<MediaListEntry>?
)

data class MediaListEntry(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String?,
    @SerializedName("score") val score: Double?,
    @SerializedName("progress") val progress: Int,
    @SerializedName("updatedAt") val updatedAt: Long?,
    @SerializedName("media") val media: AniListMedia?
)

data class ViewerData(
    @SerializedName("Viewer") val viewer: AniListUser?
)

data class AniListUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("avatar") val avatar: UserAvatar?
)

data class UserAvatar(
    @SerializedName("large") val large: String?,
    @SerializedName("medium") val medium: String?
)
