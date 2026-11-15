package com.otakuzone.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.otakuzone.app.ui.navigation.OtakuZoneNavGraph
import com.otakuzone.app.ui.theme.OtakuZoneTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val initialAnimeId = intent.getIntExtra("extra_anime_id", -1).takeIf { it > 0 }
        val initialEpisode = intent.getIntExtra("extra_episode_number", -1).takeIf { it > 0 }

        setContent {
            OtakuZoneTheme {
                OtakuZoneNavGraph(
                    initialAnimeId = initialAnimeId,
                    initialEpisode = initialEpisode
                )
            }
        }
    }
}
