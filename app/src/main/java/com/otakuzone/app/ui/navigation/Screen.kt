package com.otakuzone.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Browse", Icons.Default.Explore)
    object Lists : Screen("lists", "Lists", Icons.Default.ListAlt)
    object Manga : Screen("manga", "Manga", Icons.Default.Book)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    object Detail : Screen("detail/{animeId}", "Anime Details") {
        fun createRoute(animeId: Int) = "detail/$animeId"
    }

    object Player : Screen("player/{animeId}/{episode}?idMal={idMal}", "Player") {
        fun createRoute(animeId: Int, episode: Int, idMal: Int?) =
            "player/$animeId/$episode?idMal=${idMal ?: 0}"
    }

    object MangaDetail : Screen("manga_detail/{slug}", "Manga Details") {
        fun createRoute(slug: String) = "manga_detail/$slug"
    }

    object MangaReader : Screen("manga_reader/{mangaSlug}/{chapterSlug}?title={title}&chTitle={chTitle}", "Reader") {
        fun createRoute(mangaSlug: String, chapterSlug: String, title: String, chTitle: String) =
            "manga_reader/$mangaSlug/$chapterSlug?title=${java.net.URLEncoder.encode(title, "UTF-8")}&chTitle=${java.net.URLEncoder.encode(chTitle, "UTF-8")}"
    }
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Lists,
    Screen.Manga,
    Screen.Settings
)
