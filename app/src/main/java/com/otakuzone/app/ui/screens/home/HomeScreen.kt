package com.otakuzone.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.otakuzone.app.OtakuZoneApplication
import com.otakuzone.app.data.local.WatchProgressEntity
import com.otakuzone.app.data.models.AiringScheduleEntry
import com.otakuzone.app.data.models.AniListMedia
import com.otakuzone.app.ui.components.AnimeCard
import com.otakuzone.app.ui.components.NotificationBellDialog
import com.otakuzone.app.ui.components.TopAnimeRankItem
import com.otakuzone.app.ui.theme.AccentWhite
import com.otakuzone.app.ui.theme.Background
import com.otakuzone.app.ui.theme.BorderColor
import com.otakuzone.app.ui.theme.Fg
import com.otakuzone.app.ui.theme.FgMuted
import com.otakuzone.app.ui.theme.PlayBg
import com.otakuzone.app.ui.theme.PlayFg
import com.otakuzone.app.ui.theme.StarColor
import com.otakuzone.app.ui.theme.Surface
import com.otakuzone.app.ui.theme.SurfaceCard
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAnimeClick: (Int) -> Unit,
    onContinueWatchClick: (Int, Int, Long) -> Unit,
    onSearchClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as OtakuZoneApplication
    val animeRepo = app.animeRepository
    val db = app.database
    val prefs = app.preferencesManager
    val scope = rememberCoroutineScope()

    var trendingList by remember { mutableStateOf<List<AniListMedia>>(emptyList()) }
    var popularList by remember { mutableStateOf<List<AniListMedia>>(emptyList()) }
    var airingTodayList by remember { mutableStateOf<List<AiringScheduleEntry>>(emptyList()) }
    var notificationAiringList by remember { mutableStateOf<List<AiringScheduleEntry>>(emptyList()) }

    var selectedTab by remember { mutableStateOf("Discover") }
    var topAnimeRankFilter by remember { mutableIntStateOf(0) } // 0=Today, 1=Week, 2=All time

    var isLoading by remember { mutableStateOf(true) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val continueWatchingList by db.watchProgressDao()
        .getContinueWatchingFlow()
        .collectAsState(initial = emptyList())

    fun loadData() {
        scope.launch {
            isLoading = true
            trendingList = animeRepo.getTrendingAnime().getOrNull() ?: emptyList()
            popularList = animeRepo.getPopularAnime().getOrNull() ?: emptyList()
            airingTodayList = animeRepo.getAiringToday().getOrNull() ?: emptyList()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Anikage Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ANIKAGE",
                        color = Fg,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }

                // Action Icons (Search, Notifications, Refresh)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Fg,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                notificationAiringList = animeRepo.getNotificationsAiringWindow().getOrNull() ?: emptyList()
                                showNotificationDialog = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Fg,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = { loadData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = FgMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AccentWhite,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // 1. Hero / Spotlight Section (exact anikage style)
                    val heroMedia = trendingList.firstOrNull()
                    if (heroMedia != null) {
                        item {
                            AnikageHeroSection(
                                media = heroMedia,
                                onPlayClick = { onAnimeClick(heroMedia.id) }
                            )
                        }
                    }

                    // 2. Discover Filter Bar (Discover, Season, Popular, Top rated)
                    item {
                        AnikageTabsRow(
                            selectedTab = selectedTab,
                            onTabSelect = { selectedTab = it }
                        )
                    }

                    // 3. Continue Watching (if items exist)
                    if (continueWatchingList.isNotEmpty()) {
                        item {
                            AnikageSectionTitle(title = "Continue Watching")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(continueWatchingList) { progress ->
                                    AnikageContinueWatchCard(
                                        item = progress,
                                        onClick = {
                                            onContinueWatchClick(
                                                progress.animeId,
                                                progress.episodeNumber,
                                                progress.positionMs
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Trending / Discover row
                    val displayList = when (selectedTab) {
                        "Popular" -> popularList
                        "Top rated" -> popularList.sortedByDescending { it.averageScore ?: 0 }
                        else -> trendingList
                    }

                    if (displayList.isNotEmpty()) {
                        item {
                            AnikageSectionTitle(title = selectedTab)
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(displayList) { media ->
                                    AnimeCard(
                                        media = media,
                                        onClick = { onAnimeClick(media.id) }
                                    )
                                }
                            }
                        }
                    }

                    // 5. Airing Today row
                    if (airingTodayList.isNotEmpty()) {
                        item {
                            AnikageSectionTitle(title = "Airing Today")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(airingTodayList) { schedule ->
                                    val media = schedule.media ?: return@items
                                    AnimeCard(
                                        media = media,
                                        onClick = { onAnimeClick(media.id) }
                                    )
                                }
                            }
                        }
                    }

                    // 6. Top anime (Ranked 1-10) with Today / Week / All time toggle
                    val topRankedList = popularList.take(10)
                    if (topRankedList.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Top anime",
                                    color = Fg,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceCard)
                                        .padding(2.dp)
                                ) {
                                    listOf("Today", "Week", "All time").forEachIndexed { index, title ->
                                        val isSelected = topAnimeRankFilter == index
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) Color(0xFF2B2B2B) else Color.Transparent)
                                                .clickable { topAnimeRankFilter = index }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = title,
                                                color = if (isSelected) Fg else FgMuted,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(topRankedList.chunked(1)) { chunk ->
                            val index = topRankedList.indexOf(chunk[0]) + 1
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                TopAnimeRankItem(
                                    rank = index,
                                    media = chunk[0],
                                    onClick = { onAnimeClick(chunk[0].id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Notification Dialog
        if (showNotificationDialog) {
            NotificationBellDialog(
                airingList = notificationAiringList,
                isLoading = false,
                isSubscribed = { prefs.isNotificationEnabledForAnime(it) },
                onToggleSubscribe = { id, enabled ->
                    prefs.setNotificationEnabledForAnime(id, enabled)
                },
                onAnimeClick = { onAnimeClick(it) },
                onDismiss = { showNotificationDialog = false }
            )
        }
    }
}

@Composable
private fun AnikageSectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Fg,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "View all",
            color = FgMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AnikageTabsRow(
    selectedTab: String,
    onTabSelect: (String) -> Unit
) {
    val tabs = listOf("Discover", "Season", "Popular", "Top rated")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) SurfaceCard else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) BorderColor else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onTabSelect(tab) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) Fg else FgMuted,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun AnikageHeroSection(
    media: AniListMedia,
    onPlayClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        // Full bleed background backdrop image
        AsyncImage(
            model = media.bannerImage ?: media.getBestCover(),
            contentDescription = media.getDisplayName(),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient fade to bottom & sides
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x660A0A0A),
                            Color(0xCC0A0A0A),
                            Background
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Badges row (New, Ep N, Format, Score, Airing)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE11D48))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "New",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (media.nextAiringEpisode != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x99000000))
                            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Ep ${media.nextAiringEpisode.episode}",
                            color = Fg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x99000000))
                        .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = media.format ?: "TV",
                        color = FgMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (media.averageScore != null && media.averageScore > 0) {
                    val score = String.format("%.1f", media.averageScore / 10.0)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x99000000))
                            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = StarColor,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = score,
                                color = Fg,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (media.status == "RELEASING") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x99000000))
                            .border(1.dp, Color(0xFF16A34A), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Airing",
                            color = Color(0xFF22C55E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Title
            Text(
                text = media.getDisplayName(),
                color = Fg,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: "Play now" white pill button + icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlayBg,
                        contentColor = PlayFg
                    ),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = PlayFg,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play now",
                        color = PlayFg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bookmark icon button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderColor, CircleShape)
                        .clickable { /* Bookmark */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = Fg,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Favorite icon button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderColor, CircleShape)
                        .clickable { /* Favorite */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Fg,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Share icon button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderColor, CircleShape)
                        .clickable { /* Share */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Fg,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnikageContinueWatchCard(
    item: WatchProgressEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .background(SurfaceCard)
        ) {
            AsyncImage(
                model = item.coverUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Centered Play Pill
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC000000))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Resume",
                    tint = AccentWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Episode Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "EP ${item.episodeNumber}",
                    color = Fg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Progress Bar
            if (item.durationMs > 0) {
                val progressRatio = (item.positionMs.toFloat() / item.durationMs.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = AccentWhite,
                    trackColor = Color(0x66000000)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.title,
            color = Fg,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val posMinutes = (item.positionMs / 60000)
        val posSeconds = (item.positionMs % 60000) / 1000
        Text(
            text = "${posMinutes}m ${posSeconds}s remaining",
            color = FgMuted,
            fontSize = 11.sp
        )
    }
}
