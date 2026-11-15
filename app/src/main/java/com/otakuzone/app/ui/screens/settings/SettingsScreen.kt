package com.otakuzone.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.otakuzone.app.BuildConfig
import com.otakuzone.app.OtakuZoneApplication
import com.otakuzone.app.ui.theme.AccentWhite
import com.otakuzone.app.ui.theme.Background
import com.otakuzone.app.ui.theme.BorderColor
import com.otakuzone.app.ui.theme.Danger
import com.otakuzone.app.ui.theme.Fg
import com.otakuzone.app.ui.theme.FgMuted
import com.otakuzone.app.ui.theme.PlayBg
import com.otakuzone.app.ui.theme.PlayFg
import com.otakuzone.app.ui.theme.SurfaceCard
import com.otakuzone.app.ui.theme.SurfaceInput
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as OtakuZoneApplication
    val prefs = app.preferencesManager
    val syncRepo = app.syncRepository
    val scope = rememberCoroutineScope()

    var pinCode by remember { mutableStateOf("") }
    var isAuthenticating by remember { mutableStateOf(false) }
    var mangaVaultUrl by remember { mutableStateOf(prefs.mangaVaultUrl) }
    var notificationsEnabled by remember { mutableStateOf(prefs.notificationsEnabled) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Text(
            text = "Settings",
            color = Fg,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 1. AniList Account (OAuth PIN flow)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AccentWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AniList Integration (OAuth PIN)",
                    color = Fg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (syncRepo.isLoggedIn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!prefs.anilistUserAvatar.isNullOrBlank()) {
                            AsyncImage(
                                model = prefs.anilistUserAvatar,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column {
                            Text(
                                text = prefs.anilistUsername ?: "Connected",
                                color = Fg,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Lists synchronized",
                                color = Color(0xFF22C55E),
                                fontSize = 12.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            syncRepo.logout()
                            Toast.makeText(context, "Logged out from AniList", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Danger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout", color = Danger, fontSize = 12.sp)
                    }
                }
            } else {
                Text(
                    text = "Connect your AniList account to sync watching progress, air notifications, and anime lists.",
                    color = FgMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val authUrl = "https://anilist.co/api/v2/oauth/authorize?client_id=${BuildConfig.ANILIST_CLIENT_ID}&response_type=code"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlayBg,
                        contentColor = PlayFg
                    ),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        tint = PlayFg,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("1. Open AniList & Get PIN", color = PlayFg, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pinCode,
                    onValueChange = { pinCode = it },
                    placeholder = { Text("Paste authorization PIN code here", color = FgMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = FgMuted, modifier = Modifier.size(16.dp))
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

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (pinCode.isBlank()) return@Button
                        scope.launch {
                            isAuthenticating = true
                            val res = syncRepo.loginWithPin(pinCode)
                            isAuthenticating = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "Successfully connected to AniList!", Toast.LENGTH_LONG).show()
                                pinCode = ""
                            } else {
                                Toast.makeText(context, "Login failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = pinCode.isNotBlank() && !isAuthenticating,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(color = AccentWhite, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("2. Authorize with PIN", color = Fg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Notifications
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AccentWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Phone Notifications",
                            color = Fg,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Periodic airing schedule checks via WorkManager",
                            color = FgMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        prefs.notificationsEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = AccentWhite,
                        uncheckedThumbColor = FgMuted,
                        uncheckedTrackColor = Color(0xFF262626)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. MangaVault Backend Configuration
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = AccentWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MangaVault Backend URL",
                    color = Fg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Configurable endpoint for MangaVault API (FastAPI backend). Default is http://localhost:8000.",
                color = FgMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = mangaVaultUrl,
                onValueChange = {
                    mangaVaultUrl = it
                    prefs.mangaVaultUrl = it
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
        }
    }
}
