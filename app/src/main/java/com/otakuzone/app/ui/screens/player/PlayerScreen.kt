package com.otakuzone.app.ui.screens.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.otakuzone.app.OtakuZoneApplication
import com.otakuzone.app.data.local.WatchProgressEntity
import com.otakuzone.app.data.models.AudioMode
import com.otakuzone.app.data.models.StreamServer
import com.otakuzone.app.ui.components.ServerPickerBottomSheet
import com.otakuzone.app.ui.theme.AccentError
import com.otakuzone.app.ui.theme.AccentPrimary
import com.otakuzone.app.ui.theme.DarkBackground
import com.otakuzone.app.ui.theme.TextMuted
import com.otakuzone.app.ui.theme.TextPrimary
import com.otakuzone.app.ui.theme.TextSecondary
import com.otakuzone.app.ui.theme.ZokoBadgeColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

    // Load Anime metadata (title, cover, episodes)
    LaunchedEffect(animeId) {
        val details = animeRepo.getAnimeDetails(animeId).getOrNull()
        if (details != null) {
            animeTitle = details.getDisplayName()
            animeCover = details.getBestCover()
            totalEpisodes = details.episodes
        }
    }

    fun playStream(server: StreamServer) {
        selectedServer = server
        streamRepo.saveLastPickedServer(server.name, currentAudioMode)
        isPlayerLoading = true

        scope.launch {
            // Check resume position in Room database
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

                // Prefetch next episode's streams while user watches
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
                // Server failure handling rule:
                // Mark server failed and auto-jump to next server, showing message
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

    // Position progress tracking loop: saves second-precision resume point and pushes at 75%
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            delay(1000)
            if (exoPlayer.isPlaying) {
                val position = exoPlayer.currentPosition
                val duration = exoPlayer.duration
                if (duration > 0 && position > 0) {
                    val progressRatio = position.toFloat() / duration.toFloat()
                    val is75Percent = progressRatio >= 0.75f

                    // Save local progress in Room
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

                    // Auto-mark watched on AniList at 75%
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
        containerColor = DarkBackground
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
                    .aspectRatio(if (isFullscreen) 16f / 9f else 16f / 9f)
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
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPrimary)
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
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            isFullscreen = !isFullscreen
                            activity?.requestedOrientation = if (isFullscreen) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                }
            }

            // Player Meta and Server Controls
            if (!isFullscreen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "$animeTitle — Episode $currentEpisode",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Current Server and Quick Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Server: ${selectedServer?.name ?: "None"} (${selectedServer?.format ?: ""})",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            if (selectedServer?.isDefault == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DEFAULT",
                                    color = ZokoBadgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Next Episode Button
                        IconButton(
                            onClick = {
                                currentEpisode += 1
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Episode",
                                tint = AccentPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Open Server Picker Button
                    OutlinedButton(
                        onClick = { showServerPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = AccentPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Change Server (${servers.size} available)",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Server Picker BottomSheet
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
