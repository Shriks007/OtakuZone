package com.otakuzone.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.otakuzone.app.OtakuZoneApplication
import com.otakuzone.app.data.models.AniListMedia
import com.otakuzone.app.ui.theme.AccentPrimary
import com.otakuzone.app.ui.theme.AccentSuccess
import com.otakuzone.app.ui.theme.AccentWarning
import com.otakuzone.app.ui.theme.DarkBackground
import com.otakuzone.app.ui.theme.DarkCardElevated
import com.otakuzone.app.ui.theme.DarkSurface
import com.otakuzone.app.ui.theme.TextMuted
import com.otakuzone.app.ui.theme.TextPrimary
import com.otakuzone.app.ui.theme.TextSecondary
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
    var isLoading by remember { mutableStateOf(true) }
    var currentStatus by remember { mutableStateOf<String?>(null) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(animeId) {
        isLoading = true
        val result = animeRepo.getAnimeDetails(animeId)
        media = result.getOrNull()
        currentStatus = media?.mediaListEntry?.status
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentPrimary)
        }
        return
    }

    val item = media ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Failed to load anime details", color = TextMuted)
        }
        return
    }

    val totalEps = item.episodes ?: 24 // Fallback if ongoing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Header Banner & Poster
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    AsyncImage(
                        model = item.bannerImage ?: item.getBestCover(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        DarkBackground.copy(alpha = 0.7f),
                                        DarkBackground
                                    )
                                )
                            )
                    )

                    // Back button
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    // Poster thumbnail & Meta
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AsyncImage(
                            model = item.getBestCover(),
                            contentDescription = item.getDisplayName(),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp, 160.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = item.getDisplayName(),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.averageScore != null) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AccentWarning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${item.averageScore}%",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Text(
                                    text = item.status ?: "",
                                    color = AccentSuccess,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // MAL ID badge
                            if (item.idMal != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "MAL ID: ${item.idMal}",
                                        color = AccentPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Action Buttons (Watch Ep 1, AniList List Selector)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onPlayEpisode(item.id, 1, item.idMal) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch Ep 1", fontWeight = FontWeight.Bold)
                    }

                    Box {
                        OutlinedButton(
                            onClick = { showStatusDropdown = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = AccentPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentStatus ?: "Add to List", color = TextPrimary)
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            listOf("CURRENT", "PLANNING", "COMPLETED", "DROPPED", "PAUSED").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status, color = TextPrimary) },
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
                }
            }

            // 3. Genres
            if (!item.genres.isNullOrEmpty()) {
                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item.genres.forEach { genre ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkCardElevated)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = genre, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 4. Synopsis
            if (!item.description.isNullOrBlank()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "Synopsis",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val cleanDesc = item.description.replace(Regex("<.*?>"), "").trim()
                        Text(
                            text = cleanDesc,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // 5. Episode List Header & Grid
            item {
                Text(
                    text = "Episodes ($totalEps)",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (ep in 1..totalEps) {
                        EpisodeGridButton(
                            episode = ep,
                            onClick = { onPlayEpisode(item.id, ep, item.idMal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeGridButton(
    episode: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp, 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCardElevated)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$episode",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
