package com.otakuzone.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otakuzone.app.data.models.AudioMode
import com.otakuzone.app.data.models.StreamServer
import com.otakuzone.app.ui.theme.AccentWhite
import com.otakuzone.app.ui.theme.BorderColor
import com.otakuzone.app.ui.theme.Danger
import com.otakuzone.app.ui.theme.DubBadgeColor
import com.otakuzone.app.ui.theme.Fg
import com.otakuzone.app.ui.theme.FgMuted
import com.otakuzone.app.ui.theme.SubBadgeColor
import com.otakuzone.app.ui.theme.Surface
import com.otakuzone.app.ui.theme.SurfaceCard
import com.otakuzone.app.ui.theme.SurfaceInput
import com.otakuzone.app.ui.theme.ZokoBadgeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerPickerBottomSheet(
    servers: List<StreamServer>,
    selectedServer: StreamServer?,
    currentAudioMode: AudioMode,
    onAudioModeChange: (AudioMode) -> Unit,
    onServerSelect: (StreamServer) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stream Servers",
                    color = Fg,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = FgMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Audio Mode Toggle: SUB / DUB
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = currentAudioMode == AudioMode.SUB,
                    onClick = { onAudioModeChange(AudioMode.SUB) },
                    label = { Text("SUB Audio", fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SubBadgeColor.copy(alpha = 0.2f),
                        selectedLabelColor = SubBadgeColor,
                        containerColor = SurfaceCard,
                        labelColor = FgMuted
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = currentAudioMode == AudioMode.DUB,
                    onClick = { onAudioModeChange(AudioMode.DUB) },
                    label = { Text("DUB Audio", fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DubBadgeColor.copy(alpha = 0.2f),
                        selectedLabelColor = DubBadgeColor,
                        containerColor = SurfaceCard,
                        labelColor = FgMuted
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar for servers
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search server (e.g. Zoko, Zoro, Vid)", color = FgMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = FgMuted)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceInput,
                    unfocusedContainerColor = SurfaceInput,
                    focusedTextColor = Fg,
                    unfocusedTextColor = Fg,
                    cursorColor = AccentWhite,
                    focusedIndicatorColor = AccentWhite,
                    unfocusedIndicatorColor = BorderColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter servers by mode & search
            val filteredServers = remember(servers, currentAudioMode, searchQuery) {
                servers.filter { server ->
                    val matchesMode = if (currentAudioMode == AudioMode.SUB) server.isSub else server.isDub
                    val matchesSearch = searchQuery.isBlank() ||
                            server.name.contains(searchQuery, ignoreCase = true) ||
                            server.format.contains(searchQuery, ignoreCase = true)
                    matchesMode && matchesSearch
                }
            }

            if (filteredServers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${currentAudioMode.name} servers found",
                        color = FgMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredServers) { server ->
                        val isSelected = selectedServer?.name == server.name &&
                                selectedServer.format == server.format

                        ServerItemRow(
                            server = server,
                            isSelected = isSelected,
                            onClick = {
                                onServerSelect(server)
                                onDismiss()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ServerItemRow(
    server: StreamServer,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> AccentWhite
        server.isFailed -> Danger.copy(alpha = 0.5f)
        else -> BorderColor
    }

    val backgroundColor = when {
        isSelected -> Color(0xFF222222)
        server.isFailed -> SurfaceCard.copy(alpha = 0.4f)
        else -> SurfaceCard
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = !server.isFailed) { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = server.name,
                color = if (server.isFailed) FgMuted else Fg,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Format Badge (v-sub, h-sub, etc.)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = server.format.uppercase(),
                    color = FgMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Default Badge (Zoko)
            if (server.isDefault) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ZokoBadgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "DEFAULT",
                        color = ZokoBadgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (server.isFailed) {
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Danger,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Failed",
                        color = Danger,
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                .clip(CircleShape)
                .background(AccentWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.Black,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
