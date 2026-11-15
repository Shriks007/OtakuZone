package com.otakuzone.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.otakuzone.app.BuildConfig
import com.otakuzone.app.data.models.AudioMode

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "otaku_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("otaku_fallback_prefs", Context.MODE_PRIVATE)
    }

    var anilistToken: String?
        get() = prefs.getString("anilist_token", null)
        set(value) = prefs.edit().putString("anilist_token", value).apply()

    var anilistUsername: String?
        get() = prefs.getString("anilist_username", null)
        set(value) = prefs.edit().putString("anilist_username", value).apply()

    var anilistUserAvatar: String?
        get() = prefs.getString("anilist_user_avatar", null)
        set(value) = prefs.edit().putString("anilist_user_avatar", value).apply()

    var anilistUserId: Int
        get() = prefs.getInt("anilist_user_id", -1)
        set(value) = prefs.edit().putInt("anilist_user_id", value).apply()

    var mangaVaultUrl: String
        get() = prefs.getString("mangavault_url", BuildConfig.MANGAVAULT_DEFAULT_URL) ?: BuildConfig.MANGAVAULT_DEFAULT_URL
        set(value) = prefs.edit().putString("mangavault_url", value.trimEnd('/')).apply()

    var lastServerName: String?
        get() = prefs.getString("last_server_name", null)
        set(value) = prefs.edit().putString("last_server_name", value).apply()

    var lastAudioMode: AudioMode
        get() {
            val modeStr = prefs.getString("last_audio_mode", AudioMode.SUB.name)
            return try {
                AudioMode.valueOf(modeStr ?: AudioMode.SUB.name)
            } catch (e: Exception) {
                AudioMode.SUB
            }
        }
        set(value) = prefs.edit().putString("last_audio_mode", value.name).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    fun isNotificationEnabledForAnime(animeId: Int): Boolean {
        val disabledSet = prefs.getStringSet("disabled_notifications_anime", emptySet()) ?: emptySet()
        return !disabledSet.contains(animeId.toString())
    }

    fun setNotificationEnabledForAnime(animeId: Int, enabled: Boolean) {
        val currentSet = prefs.getStringSet("disabled_notifications_anime", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (enabled) {
            currentSet.remove(animeId.toString())
        } else {
            currentSet.add(animeId.toString())
        }
        prefs.edit().putStringSet("disabled_notifications_anime", currentSet).apply()
    }

    fun clearAuth() {
        prefs.edit()
            .remove("anilist_token")
            .remove("anilist_username")
            .remove("anilist_user_avatar")
            .remove("anilist_user_id")
            .apply()
    }
}
