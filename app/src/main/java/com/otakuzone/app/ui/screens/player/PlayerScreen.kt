package com.otakuzone.app.ui.screens.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.otakuzone.app.OtakuZoneApplication
import com.otakuzone.app.data.local.WatchProgressEntity
import com.otakuzone.app.data.models.AudioMode
import com.otakuzone.app.data.models.StreamServer
import com.otakuzone.app.ui.components.ServerPickerBottomSheet
import com.otakuzone.app.ui.theme.AccentWhite
import com.otakuzone.app.ui.theme.Background
import com.otakuzone.app.ui.theme.BorderColor
import com.otakuzone.app.ui.theme.Danger
import com.otakuzone.app.ui.theme.DubBadgeColor
import com.otakuzone.app.ui.theme.Fg
import com.otakuzone.app.ui.theme.FgMuted
import com.otakuzone.app.ui.theme.SubBadgeColor
import com.otakuzone.app.ui.theme.SurfaceCard
import com.otakuzone.app.ui.theme.SurfaceElevated
import com.otakuzone.app.ui.theme.ZokoBadgeColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun PlayerScreen(
    animeId: Int,
    initialEpisode: Int,
    idMal: Int?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val app = context.applicationContext as OtakuZoneApplication
    val streamRepo = app.streamRepository
    val animeRepo = app.animeRepository
    val syncRepo = app.syncRepository
    val db = app.database
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentEpisode by remember { mutableStateOf(initialEpisode) }
    var servers by remember { mutableStateOf<List<StreamServer>>(emptyList()) }
    var selectedServer by remember { mutableStateOf<StreamServer?>(null) }
    var currentAudioMode by remember { mutableStateOf(streamRepo.getLastAudioMode()) }
    var isLoadingStreams by remember { mutableStateOf(true) }
    var isPlayerLoading by remember { mutableStateOf(true) }
    var showServerPicker by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var animeTitle by remember { mutableStateOf("Anime") }
    var animeCover by remember { mutableStateOf("") }
    var totalEpisodes by remember { mutableStateOf<Int?>(null) }
    var hasMarkedWatchedAt75 by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Load Anime metadata
    LaunchedEffect(animeId) {
        val details = animeRepo.getAnimeDetails(animeId).getOrNull()
        if (details != null) {
            animeTitle = details.getDisplayName()
            animeCover = details.getBestCover()
            totalEpisodes = details.episodes ?: details.nextAiringEpisode?.episode?.minus(1)
        }
    }

    fun playStream(server: StreamServer) {
        selectedServer = server
        streamRepo.saveLastPickedServer(server.name, currentAudioMode)
        isPlayerLoading = true

        scope.launch {
            val savedProgress = db.watchProgressDao().getProgress(animeId, currentEpisode)
            val startPositionMs = savedProgress?.positionMs ?: 0L

            val mediaItem = MediaItem.fromUri(Uri.parse(server.url))
            exoPlayer.setMediaItem(mediaItem, startPositionMs)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    fun loadStreamsForEpisode(ep: Int) {
        scope.launch {
            isLoadingStreams = true
            hasMarkedWatchedAt75 = false

            val result = streamRepo.getStreams(animeId, ep, idMal)
            val serverList = result.getOrNull() ?: emptyList()
            servers = serverList

            if (serverList.isNotEmpty()) {
                val initialServer = streamRepo.pickInitialServer(serverList, currentAudioMode)
                if (initialServer != null) {
                    playStream(initialServer)
                } else {
                    snackbarHostState.showSnackbar("No available servers found for Episode $ep")
                }

                // Prefetch next episode streams
                streamRepo.prefetchNextEpisode(animeId, ep + 1, idMal)
            } else {
                snackbarHostState.showSnackbar("Could not load streams for Episode $ep")
            }
            isLoadingStreams = false
        }
    }

    LaunchedEffect(currentEpisode) {
        loadStreamsForEpisode(currentEpisode)
    }

    // Player event listeners: auto-fallback on error & 75% watched tracking
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isPlayerLoading = playbackState == Player.STATE_BUFFERING
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                val failedServer = selectedServer
                if (failedServer != null) {
                    val nextServer = streamRepo.getNextFallbackServer(failedServer, servers, currentAudioMode)
                    if (nextServer != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Server error — trying ${nextServer.name} (${nextServer.format})")
                            playStream(nextServer)
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("All servers failed for Episode $currentEpisode")
                        }
                    }
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Position progress tracking loop: 75% auto mark watched + resume store
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            delay(1000)
            if (exoPlayer.isPlaying) {
                val position = exoPlayer.currentPosition
                val duration = exoPlayer.duration
                if (duration > 0 && position > 0) {
                    val progressRatio = position.toFloat() / duration.toFloat()
                    val is75Percent = progressRatio >= 0.75f

                    db.watchProgressDao().saveProgress(
                        WatchProgressEntity(
                            animeId = animeId,
                            episodeNumber = currentEpisode,
                            positionMs = position,
                            durationMs = duration,
                            isCompleted = is75Percent,
                            title = animeTitle,
                            coverUrl = animeCover
                        )
                    )

                    if (is75Percent && !hasMarkedWatchedAt75) {
                        hasMarkedWatchedAt75 = true
                        syncRepo.syncWatchProgress(
                            mediaId = animeId,
                            progress = currentEpisode,
                            totalEpisodes = totalEpisodes
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Video Player Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            setShowNextButton(false)
                            setShowPreviousButton(false)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isPlayerLoading || isLoadingStreams) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AccentWhite,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Top Controls Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (isFullscreen) {
                                isFullscreen = false
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            } else {
                                onBackClick()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Fg,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            isFullscreen = !isFullscreen
                            activity?.requestedOrientation = if (isFullscreen) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Fg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Anikage Player Bottom Bar & Episode Switcher
            if (!isFullscreen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title & Episode
                    Text(
                        text = "$animeTitle — Episode $currentEpisode",
                        color = Fg,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Audio Mode Switcher (SUB / DUB) & Active Server Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick SUB/DUB toggle chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (currentAudioMode == AudioMode.SUB) Color(0xFF1F2937) else SurfaceCard)
                                    .border(
                                        1.dp,
                                        if (currentAudioMode == AudioMode.SUB) SubBadgeColor else BorderColor,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        currentAudioMode = AudioMode.SUB
                                        val nextServer = streamRepo.pickInitialServer(servers, AudioMode.SUB)
                                        if (nextServer != null) playStream(nextServer)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ClosedCaption,
                                        contentDescription = "SUB",
                                        tint = SubBadgeColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SUB",
                                        color = if (currentAudioMode == AudioMode.SUB) Fg else FgMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (currentAudioMode == AudioMode.DUB) Color(0xFF064E3B) else SurfaceCard)
                                    .border(
                                        1.dp,
                                        if (currentAudioMode == AudioMode.DUB) DubBadgeColor else BorderColor,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        currentAudioMode = AudioMode.DUB
                                        val nextServer = streamRepo.pickInitialServer(servers, AudioMode.DUB)
                                        if (nextServer != null) playStream(nextServer)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "DUB",
                                        tint = DubBadgeColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "DUB",
                                        color = if (currentAudioMode == AudioMode.DUB) Fg else FgMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Prev / Next Episode Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { if (currentEpisode > 1) currentEpisode -= 1 },
                                enabled = currentEpisode > 1,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Prev Ep",
                                    tint = if (currentEpisode > 1) Fg else FgMuted
                                )
                            }

                            IconButton(
                                onClick = { currentEpisode += 1 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Ep",
                                    tint = Fg
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Server Selection Button (exact Anikage card styling)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clickable { showServerPicker = true }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = AccentWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Server: ${selectedServer?.name ?: "Auto"} (${selectedServer?.format ?: ""})",
                                color = Fg,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (selectedServer?.isDefault == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(ZokoBadgeColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "DEFAULT",
                                        color = ZokoBadgeColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Switch (${servers.size})",
                            color = FgMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Episodes Quick Switcher Horizontal List
                    val total = totalEpisodes ?: 24
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Episodes",
                        color = Fg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((1..total).toList()) { ep ->
                            val isCurrent = ep == currentEpisode
                            Box(
                                modifier = Modifier
                                    .size(46.dp, 36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrent) AccentWhite else SurfaceCard)
                                    .border(
                                        1.dp,
                                        if (isCurrent) AccentWhite else BorderColor,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { currentEpisode = ep },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$ep",
                                    color = if (isCurrent) Color.Black else Fg,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Server Picker Sheet
        if (showServerPicker) {
            ServerPickerBottomSheet(
                servers = servers,
                selectedServer = selectedServer,
                currentAudioMode = currentAudioMode,
                onAudioModeChange = { newMode ->
                    currentAudioMode = newMode
                    val nextServer = streamRepo.pickInitialServer(servers, newMode)
                    if (nextServer != null) {
                        playStream(nextServer)
                    }
                },
                onServerSelect = { server ->
                    playStream(server)
                },
                onDismiss = { showServerPicker = false },
                sheetState = sheetState
            )
        }
    }
}
