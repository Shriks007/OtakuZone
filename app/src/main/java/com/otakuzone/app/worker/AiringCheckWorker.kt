package com.otakuzone.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.otakuzone.app.MainActivity
import com.otakuzone.app.OtakuZoneApplication
import com.otakuzone.app.R
import java.util.concurrent.TimeUnit

class AiringCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "OtakuZoneAiringCheckWorker"
        const val CHANNEL_ID = "otakuzone_airing_channel"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<AiringCheckWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val app = context.applicationContext as? OtakuZoneApplication ?: return Result.success()
        val prefs = app.preferencesManager
        val animeRepo = app.animeRepository
        val syncRepo = app.syncRepository

        if (!prefs.notificationsEnabled) {
            return Result.success()
        }

        try {
            createNotificationChannel()

            // Fetch user's watching lists
            val userLists = if (syncRepo.isLoggedIn) {
                syncRepo.getUserLists().getOrNull() ?: emptyList()
            } else {
                emptyList()
            }

            val watchlistedIds = userLists
                .filter { it.status.equals("CURRENT", ignoreCase = true) || it.status.equals("PLANNING", ignoreCase = true) }
                .flatMap { it.entries ?: emptyList() }
                .mapNotNull { it.media?.id }
                .toSet()

            val schedules = animeRepo.getNotificationsAiringWindow().getOrNull() ?: emptyList()
            val nowSeconds = System.currentTimeMillis() / 1000

            for (entry in schedules) {
                val media = entry.media ?: continue
                val animeId = media.id

                // If user is logged in, filter by their watchlist; otherwise notify for subscribed titles
                val isRelevant = if (watchlistedIds.isNotEmpty()) {
                    watchlistedIds.contains(animeId)
                } else {
                    prefs.isNotificationEnabledForAnime(animeId)
                }

                if (isRelevant && prefs.isNotificationEnabledForAnime(animeId)) {
                    // Check if aired recently (within last 3 hours)
                    val diff = nowSeconds - entry.airingAt
                    if (diff in 0..10800) {
                        showNotification(
                            animeId = animeId,
                            title = media.getDisplayName(),
                            episode = entry.episode
                        )
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_airing_name)
            val descriptionText = context.getString(R.string.channel_airing_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(animeId: Int, title: String, episode: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("extra_anime_id", animeId)
            putExtra("extra_episode_number", episode)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            animeId * 1000 + episode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("New Episode Aired! ⛩️")
            .setContentText("$title — Episode $episode is now available")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title — Episode $episode has just aired and is ready to stream!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(animeId * 1000 + episode, notification)
    }
}
