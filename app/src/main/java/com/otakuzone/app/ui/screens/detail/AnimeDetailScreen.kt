package com.otakuzone.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.otakuzone.app.data.models.AniListMedia
import com.otakuzone.app.ui.components.AnimeCard
import com.otakuzone.app.ui.theme.AccentWhite
import com.otakuzone.app.ui.theme.Background
import com.otakuzone.app.ui.theme.BorderColor
import com.otakuzone.app.ui.theme.Danger
import com.otakuzone.app.ui.theme.DubBadgeColor
import com.otakuzone.app.ui.theme.Fg
import com.otakuzone.app.ui.theme.FgMuted
import com.otakuzone.app.ui.theme.PlayBg
import com.otakuzone.app.ui.theme.PlayFg
import com.otakuzone.app.ui.theme.StarColor
import com.otakuzone.app.ui.theme.SubBadgeColor
import com.otakuzone.app.ui.theme.Surface
import com.otakuzone.app.ui.theme.SurfaceCard
import com.otakuzone.app.ui.theme.SurfaceElevated
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailScreen(
    animeId: Int,
    onBackClick: () -> Unit,
    onPlayEpisode: (Int, Int, Int?) -> Unit // animeId, episode, idMal
) {
    val context = LocalContext.current
    val app = context.applicationContext as OtakuZoneApplication
    val animeRepo = app.animeRepository
    val syncRepo = app.syncRepository
    val scope = rememberCoroutineScope()

    var media by remember { mutableStateOf<AniListMedia?>(null) }
    var recommendations by remember { mutableStateOf<List<AniListMedia>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentStatus by remember { mutableStateOf<String?>(null) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf("Overview") }
    var isGridView by remember { mutableStateOf(false) }

    LaunchedEffect(animeId) {
        isLoading = true
        val result = animeRepo.getAnimeDetails(animeId)
        media = result.getOrNull()
        currentStatus = media?.mediaListEntry?.status
        // Also load trending for recommendations fallback
        recommendations = animeRepo.getTrendingAnime().getOrNull()?.filter { it.id != animeId }?.take(8) ?: emptyList()
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = AccentWhite,
                strokeWidth = 2.dp,
                modifier = Modifier.size(36.dp)
            )
        }
        return
    }

    val item = media ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            Text("Failed to load anime details", color = FgMuted)
        }
        return
    }

    val totalEps = item.episodes ?: item.nextAiringEpisode?.episode?.minus(1) ?: 12

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Header Banner & Info (exact Anikage layout)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // Full-width banner backdrop
                    AsyncImage(
                        model = item.bannerImage ?: item.getBestCover(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient fade to background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x800A0A0A),
                                        Color(0xCC0A0A0A),
                                        Background
                                    )
                                )
                            )
                    )

                    // Top navigation bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                                .border(1.dp, BorderColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Fg,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { /* Share */ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                                    .border(1.dp, BorderColor, CircleShape)
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

                    // Poster thumbnail & Meta row
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Poster with border
                        AsyncImage(
                            model = item.getBestCover(),
                            contentDescription = item.getDisplayName(),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(105.dp)
                                .aspectRatio(0.70f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            // Title
                            Text(
                                text = item.getDisplayName(),
                                color = Fg,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 24.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Score & Status badges row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (item.averageScore != null && item.averageScore > 0) {
                                    val score = String.format("%.1f", item.averageScore / 10.0)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Danger)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = score,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF262626))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.status ?: "UNKNOWN",
                                        color = Fg,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "$totalEps Episodes",
                                    color = FgMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. Play Now CTA + Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "Play now" white pill button
                    Button(
                        onClick = { onPlayEpisode(item.id, 1, item.idMal) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PlayBg,
                            contentColor = PlayFg
                        ),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1f)
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

                    // Add to list status dropdown
                    Box {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceCard)
                                .border(1.dp, BorderColor, CircleShape)
                                .clickable { showStatusDropdown = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Add to list",
                                tint = Fg,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false },
                            modifier = Modifier.background(SurfaceElevated)
                        ) {
                            listOf("CURRENT", "PLANNING", "COMPLETED", "DROPPED", "PAUSED").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status, color = Fg) },
                                    onClick = {
                                        showStatusDropdown = false
                                        currentStatus = status
                                        scope.launch {
                                            syncRepo.updateMediaStatus(item.id, status)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Favorite
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
                }
            }

            // 3. Navigation Tabs (Overview, Seasons, Characters, Music)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Overview", "Seasons", "Characters", "Music").forEach { tab ->
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
                                .clickable { selectedTab = tab }
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

            // 4. Metadata Details Card (Genres, Format, Aired, Studio)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    // Genres
                    if (!item.genres.isNullOrEmpty()) {
                        Text(
                            text = "Genres",
                            color = FgMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.genres.forEach { genre ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF262626))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(text = genre, color = Fg, fontSize = 11.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Format & Episodes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Format", color = FgMuted, fontSize = 11.sp)
                            Text(text = item.format ?: "TV", color = Fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Column {
                            Text(text = "Episodes", color = FgMuted, fontSize = 11.sp)
                            Text(text = "$totalEps", color = Fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        if (item.idMal != null) {
                            Column {
                                Text(text = "MAL ID", color = FgMuted, fontSize = 11.sp)
                                Text(text = "${item.idMal}", color = Fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Synopsis
                    if (!item.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Synopsis", color = FgMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        val cleanDesc = item.description.replace(Regex("<.*?>"), "").trim()
                        Text(
                            text = cleanDesc,
                            color = Fg,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 5. Episodes Section Header (with List / Grid toggle)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Episodes",
                        color = Fg,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { isGridView = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewList,
                                contentDescription = "List view",
                                tint = if (!isGridView) Fg else FgMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { isGridView = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Grid view",
                                tint = if (isGridView) Fg else FgMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 6. Episodes Content (List View or Grid View)
            if (isGridView) {
                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (ep in 1..totalEps) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp, 40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                    .clickable { onPlayEpisode(item.id, ep, item.idMal) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$ep",
                                    color = Fg,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items((1..totalEps).toList()) { ep ->
                    EpisodeListRowItem(
                        episode = ep,
                        animeTitle = item.getDisplayName(),
                        coverUrl = item.bannerImage ?: item.getBestCover(),
                        onClick = { onPlayEpisode(item.id, ep, item.idMal) }
                    )
                }
            }

            // 7. Recommendations Section
            if (recommendations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Recommendations",
                        color = Fg,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recommendations) { rec ->
                            AnimeCard(
                                media = rec,
                                onClick = { onPlayEpisode(rec.id, 1, rec.idMal) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Anikage Episode row with landscape thumbnail, EP badge, episode title, and sub/dub icons.
 */
@Composable
private fun EpisodeListRowItem(
    episode: Int,
    animeTitle: String,
    coverUrl: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Box
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF242424))
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = "Episode $episode",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // EP Badge in corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xCC0A0A0A))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "EP $episode",
                    color = Fg,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Episode Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Episode $episode",
                color = Fg,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = animeTitle,
                color = FgMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Sub & Dub tags
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ClosedCaption,
                        contentDescription = "Sub",
                        tint = SubBadgeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "SUB", color = SubBadgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Dub",
                        tint = DubBadgeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "DUB", color = DubBadgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
