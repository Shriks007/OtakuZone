package com.otakuzone.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.otakuzone.app.ui.theme.AccentPrimary
import com.otakuzone.app.ui.theme.DarkBackground
import com.otakuzone.app.ui.theme.DarkCardElevated
import com.otakuzone.app.ui.theme.DarkSurface
import com.otakuzone.app.ui.theme.TextMuted
import com.otakuzone.app.ui.theme.TextPrimary
import com.otakuzone.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAnimeClick: (Int) -> Unit,
    onContinueWatchClick: (Int, Int, Long) -> Unit
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
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Otaku Zone",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " ⛩️",
                        fontSize = 18.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            tint = AccentPrimary
                        )
                    }

                    IconButton(onClick = { loadData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // 1. Hero / Spotlight Carousel
                    val heroMedia = trendingList.firstOrNull()
                    if (heroMedia != null) {
                        item {
                            HeroSpotlightCard(
                                media = heroMedia,
                                onClick = { onAnimeClick(heroMedia.id) }
                            )
                        }
                    }

                    // 2. Continue Watching row (with second precision resume)
                    if (continueWatchingList.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Continue Watching ⏱️")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(continueWatchingList) { progress ->
                                    ContinueWatchingCard(
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

                    // 3. Airing Today
                    if (airingTodayList.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Airing Today 📅")
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

                    // 4. Trending Anime
                    if (trendingList.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Trending Now 🔥")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(trendingList) { media ->
                                    AnimeCard(
                                        media = media,
                                        onClick = { onAnimeClick(media.id) }
                                    )
                                }
                            }
                        }
                    }

                    // 5. All-Time Popular
                    if (popularList.isNotEmpty()) {
                        item {
                            SectionHeader(title = "All-Time Popular 🌟")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(popularList) { media ->
                                    AnimeCard(
                                        media = media,
                                        onClick = { onAnimeClick(media.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notification Bell Dialog
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
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
    )
}

@Composable
private fun HeroSpotlightCard(
    media: AniListMedia,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = media.bannerImage ?: media.getBestCover(),
            contentDescription = media.getDisplayName(),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DarkBackground.copy(alpha = 0.5f),
                            DarkBackground
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = media.getDisplayName(),
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (!media.genres.isNullOrEmpty()) {
                Text(
                    text = media.genres.take(3).joinToString(" • "),
                    color = AccentPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Watch Now", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    item: WatchProgressEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                AsyncImage(
                    model = item.coverUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play icon badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = AccentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Progress Bar at bottom of thumbnail
                if (item.durationMs > 0) {
                    val progressRatio = (item.positionMs.toFloat() / item.durationMs.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter),
                        color = AccentPrimary,
                        trackColor = Color.Black.copy(alpha = 0.5f)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val posMinutes = (item.positionMs / 60000)
                val posSeconds = (item.positionMs % 60000) / 1000
                Text(
                    text = "Ep ${item.episodeNumber} • ${posMinutes}m ${posSeconds}s",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
