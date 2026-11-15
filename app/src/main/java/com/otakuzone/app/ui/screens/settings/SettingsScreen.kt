package com.otakuzone.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.otakuzone.app.BuildConfig
import com.otakuzone.app.OtakuZoneApplication
import com.otakuzone.app.ui.theme.AccentError
import com.otakuzone.app.ui.theme.AccentPrimary
import com.otakuzone.app.ui.theme.DarkBackground
import com.otakuzone.app.ui.theme.DarkCardElevated
import com.otakuzone.app.ui.theme.DarkSurface
import com.otakuzone.app.ui.theme.TextMuted
import com.otakuzone.app.ui.theme.TextPrimary
import com.otakuzone.app.ui.theme.TextSecondary
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
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Text(
            text = "Settings & Integrations",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 1. AniList Account (PIN flow)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = AccentPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AniList Integration (OAuth PIN)",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                                        .size(44.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Column {
                                Text(
                                    text = prefs.anilistUsername ?: "Connected",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Lists synchronized",
                                    color = AccentPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                syncRepo.logout()
                                Toast.makeText(context, "Logged out from AniList", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = AccentError)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Logout", color = AccentError)
                        }
                    }
                } else {
                    Text(
                        text = "Connect your AniList account to sync watching progress, air notifications, and anime lists.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val authUrl = "https://anilist.co/api/v2/oauth/authorize?client_id=${BuildConfig.ANILIST_CLIENT_ID}&response_type=code"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Open AniList & Get PIN", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { pinCode = it },
                        placeholder = { Text("Paste authorization PIN code here", color = TextMuted) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = TextSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentPrimary
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
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("2. Authorize with PIN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Notification Preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = AccentPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Phone Tray Notifications",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Local periodic checks via WorkManager (No Firebase)",
                                color = TextMuted,
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
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. MangaVault Backend Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = AccentPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MangaVault Backend URL",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Configurable endpoint for MangaVault API (FastAPI backend). Default is http://localhost:8000.",
                    color = TextSecondary,
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
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentPrimary
                    ),
                    singleLine = true
                )
            }
        }
    }
}
