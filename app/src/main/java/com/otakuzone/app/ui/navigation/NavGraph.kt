package com.otakuzone.app.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.otakuzone.app.ui.screens.detail.AnimeDetailScreen
import com.otakuzone.app.ui.screens.home.HomeScreen
import com.otakuzone.app.ui.screens.lists.MyListsScreen
import com.otakuzone.app.ui.screens.manga.MangaDetailScreen
import com.otakuzone.app.ui.screens.manga.MangaHomeScreen
import com.otakuzone.app.ui.screens.manga.MangaReaderScreen
import com.otakuzone.app.ui.screens.player.PlayerScreen
import com.otakuzone.app.ui.screens.search.SearchScreen
import com.otakuzone.app.ui.screens.settings.SettingsScreen
import com.otakuzone.app.ui.theme.AccentWhite
import com.otakuzone.app.ui.theme.Background
import com.otakuzone.app.ui.theme.BorderColor
import com.otakuzone.app.ui.theme.Fg
import com.otakuzone.app.ui.theme.NavBackground
import com.otakuzone.app.ui.theme.NavSelected
import com.otakuzone.app.ui.theme.NavUnselected

@Composable
fun OtakuZoneNavGraph(
    initialAnimeId: Int? = null,
    initialEpisode: Int? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation on Player and Reader screens
    val hideBottomBar = currentRoute?.startsWith("player") == true ||
            currentRoute?.startsWith("manga_reader") == true

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar(
                    containerColor = NavBackground,
                    contentColor = Fg,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(1.dp, BorderColor)
                ) {
                    BottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                screen.icon?.let {
                                    Icon(imageVector = it, contentDescription = screen.title)
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NavSelected,
                                selectedTextColor = NavSelected,
                                unselectedIconColor = NavUnselected,
                                unselectedTextColor = NavUnselected,
                                indicatorColor = Color(0xFF222222)
                            )
                        )
                    }
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Home
            composable(Screen.Home.route) {
                HomeScreen(
                    onAnimeClick = { animeId ->
                        navController.navigate(Screen.Detail.createRoute(animeId))
                    },
                    onContinueWatchClick = { animeId, episode, _ ->
                        navController.navigate(Screen.Player.createRoute(animeId, episode, null))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            // Search / Browse
            composable(Screen.Search.route) {
                SearchScreen(
                    onAnimeClick = { animeId ->
                        navController.navigate(Screen.Detail.createRoute(animeId))
                    },
                    onMangaClick = { slug ->
                        navController.navigate(Screen.MangaDetail.createRoute(slug))
                    }
                )
            }

            // My Lists
            composable(Screen.Lists.route) {
                MyListsScreen(
                    onAnimeClick = { animeId ->
                        navController.navigate(Screen.Detail.createRoute(animeId))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Manga Home
            composable(Screen.Manga.route) {
                MangaHomeScreen(
                    onMangaClick = { slug ->
                        navController.navigate(Screen.MangaDetail.createRoute(slug))
                    }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // Anime Detail
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("animeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
                AnimeDetailScreen(
                    animeId = animeId,
                    onBackClick = { navController.popBackStack() },
                    onPlayEpisode = { aId, ep, malId ->
                        navController.navigate(Screen.Player.createRoute(aId, ep, malId))
                    }
                )
            }

            // Video Player
            composable(
                route = Screen.Player.route,
                arguments = listOf(
                    navArgument("animeId") { type = NavType.IntType },
                    navArgument("episode") { type = NavType.IntType },
                    navArgument("idMal") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
                val episode = backStackEntry.arguments?.getInt("episode") ?: 1
                val rawMal = backStackEntry.arguments?.getInt("idMal") ?: 0
                val idMal = if (rawMal > 0) rawMal else null

                PlayerScreen(
                    animeId = animeId,
                    initialEpisode = episode,
                    idMal = idMal,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Manga Detail
            composable(
                route = Screen.MangaDetail.route,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                MangaDetailScreen(
                    slug = slug,
                    onBackClick = { navController.popBackStack() },
                    onChapterClick = { mangaSlug, chSlug, title, chTitle ->
                        navController.navigate(
                            Screen.MangaReader.createRoute(mangaSlug, chSlug, title, chTitle)
                        )
                    }
                )
            }

            // Manga Reader
            composable(
                route = Screen.MangaReader.route,
                arguments = listOf(
                    navArgument("mangaSlug") { type = NavType.StringType },
                    navArgument("chapterSlug") { type = NavType.StringType },
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("chTitle") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val mangaSlug = backStackEntry.arguments?.getString("mangaSlug") ?: ""
                val chapterSlug = backStackEntry.arguments?.getString("chapterSlug") ?: ""
                val rawTitle = backStackEntry.arguments?.getString("title") ?: ""
                val rawChTitle = backStackEntry.arguments?.getString("chTitle") ?: ""

                val title = java.net.URLDecoder.decode(rawTitle, "UTF-8")
                val chTitle = java.net.URLDecoder.decode(rawChTitle, "UTF-8")

                MangaReaderScreen(
                    mangaSlug = mangaSlug,
                    chapterSlug = chapterSlug,
                    mangaTitle = title,
                    chapterTitle = chTitle,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
