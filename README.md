# OtakuZone ⛩️

> **OtakuZone** is a high-performance native Android application for anime and manga enthusiasts, built from scratch with **Kotlin + Jetpack Compose** and **Material 3**. Target device: Nothing Phone 4a (arm64).

---

## 📱 Features

1. **Anime Streaming & Server Engine**:
   - **AniKage Worker**: Fetches streaming servers via `GET https://api.anikage.workers.dev/{anilistId}/{episode}` with 5-minute memory caching and automatic single-retry on error.
   - **Server Picker**: Expandable list with search, categorized into **SUB** (`h-sub`, `s-sub`, `e-sub`, `v-sub`) and **DUB** (`dub`, `e-dub`, `v-dub`).
   - **Zoko is Default**: Pre-selected for both sub and dub with a "DEFAULT" badge.
   - **MAL Route Resolution**: Always queries `/stream/mal/{malId}/{episode}/{sub|dub}`. The app resolves AniList ID → MAL ID (`idMal` via AniList GraphQL) prior to building the URL (preventing 404s). If an anime lacks a MAL ID, Zoko is gracefully replaced by the next available server.
   - **Auto-Failover**: On stream failure, automatically flags the server and switches to the next available server.
   - **Media3 ExoPlayer**: Seamless playback, fullscreen rotation lock, prefetching next episode servers in the background, and second-precision resume points.
   - **Auto 75% Watched Progress**: Automatically records progress locally and pushes progress to AniList once 75% of an episode is watched.

2. **AniList OAuth PIN Login & Two-Way Sync**:
   - Integrated client: `animeshrikapk` (Client ID: `57081`).
   - Strict PIN flow adhering to `https://anilist.co/api/v2/oauth/pin`.
   - Auto-import for Watching, Planning, Completed, Dropped, and Paused lists.
   - Auto-transition: When a title in **Planning** starts airing / episode 1 is watched, it moves automatically to **Watching** (`CURRENT`).
   - Manual "Sync with AniList" button for full two-way synchronization.

3. **Local Notification Tray (No Firebase / No FCM)**:
   - AndroidX **WorkManager** periodic background task (`AiringCheckWorker`).
   - Compares user watchlist and subscribed anime against AniList's airing schedule.
   - Dispatches native phone tray alerts when new episodes air.
   - Tapping an alert directly launches the episode.
   - Per-title notification alert toggles + global toggle in Settings.

4. **MangaVault Backend & Reader**:
   - Unified FastAPI manga backend bundled under `mangavault-backend/` aggregating Manganato, Atsumaru, and Comix.
   - Custom Manga Reader:
     - **Vertical-Scroll (Webtoon)** mode.
     - **Horizontal Page-by-Page** mode.
     - Pinch-to-zoom / double-tap zoom gestures.
     - Resume reading position per chapter stored in local Room database.
     - Local manga bookmarking.

---

## 🔐 Where to Put the AniList Client Secret

Secrets are never committed to version control. They are managed via `local.properties`:

1. In the project root, create or edit `local.properties`:
   ```properties
   ANILIST_CLIENT_ID=57081
   ANILIST_CLIENT_SECRET=394ps9UhZgGlIrtex7e8tfWBNNBgWJ0sPv5rmoy1
   ANILIST_REDIRECT_URI=https://anilist.co/api/v2/oauth/pin
   MANGAVAULT_DEFAULT_URL=http://localhost:8000
   ```
2. During the Gradle build, `app/build.gradle.kts` injects these properties into `BuildConfig`:
   - `BuildConfig.ANILIST_CLIENT_ID`
   - `BuildConfig.ANILIST_CLIENT_SECRET`
   - `BuildConfig.ANILIST_REDIRECT_URI`
   - `BuildConfig.MANGAVAULT_DEFAULT_URL`

For CI/CD (GitHub Actions), set the secret in your repository under:
**Settings → Secrets and variables → Actions → Repository secrets → `ANILIST_CLIENT_SECRET`**.

---

## 🚀 How to Run the MangaVault Backend

The backend is included in the [`mangavault-backend/`](./mangavault-backend/) directory.

### Prerequisites:
- Python 3.11+
- pip

### Running locally (on PC or Termux):
```bash
cd mangavault-backend
pip install fastapi uvicorn requests beautifulsoup4 curl_cffi
python api.py
```

The API will start on `http://127.0.0.1:8000`.
- In Android Emulator: use `http://10.0.2.2:8000`
- On physical phone (Nothing Phone 4a): enter your machine's local IP (e.g. `http://192.168.1.50:8000`) or run it directly in Termux at `http://localhost:8000`.
- Configure the URL inside the app under **Settings → MangaVault Backend URL**.

---

## 🔑 How Login + Notifications Were Implemented

### 1. AniList PIN Authorization Flow
1. In `SettingsScreen`, the user taps **"1. Open AniList & Get PIN"**.
2. The app opens `https://anilist.co/api/v2/oauth/authorize?client_id=57081&response_type=code` in the browser.
3. The user grants authorization; AniList displays the PIN code.
4. The user copies the code, pastes it into the app, and taps **"2. Authorize with PIN"**.
5. `AniListSyncRepository.loginWithPin` executes:
   ```http
   POST https://anilist.co/api/v2/oauth/token
   Content-Type: application/json

   {
     "grant_type": "authorization_code",
     "client_id": "57081",
     "client_secret": "<CLIENT_SECRET>",
     "redirect_uri": "https://anilist.co/api/v2/oauth/pin",
     "code": "<PIN>"
   }
   ```
6. The returned `access_token` is saved in `EncryptedSharedPreferences` (AES256-GCM).
7. `AuthInterceptor` automatically attaches `Authorization: Bearer <token>` to all subsequent AniList GraphQL requests.

### 2. Notifications Flow (No Firebase)
1. `AiringCheckWorker` is enqueued as a unique periodic task via AndroidX `WorkManager`.
2. Every interval, `AiringCheckWorker` queries AniList GraphQL for airing schedules within the 24-hour window.
3. For anime in the user's watchlist (`Watching` / `Planning`), it checks if `isNotificationEnabledForAnime(animeId)` is enabled.
4. When a new episode airs, it fires an Android `NotificationCompat` with high priority.
5. Tapping the notification triggers `MainActivity` with intent extras `extra_anime_id` and `extra_episode_number`, opening the player directly.

---

## 📦 Building the Debug APK

The repository includes a GitHub Actions workflow `.github/workflows/build-apk.yml`.
- Pushing to `main` triggers a complete Gradle build on Ubuntu runners with JDK 17 and Android SDK 34.
- Produces `app-debug.apk` optimized for `arm64-v8a` (Nothing Phone 4a).
- Automatically uploads the APK artifact and creates a GitHub Release:
  **[Releases / OtakuZone v1.0.0 Debug APK](https://github.com/Shriks007/OtakuZone/releases)**.

To build locally:
```bash
chmod +x gradlew
./gradlew assembleDebug
```
Output location:
`app/build/outputs/apk/debug/app-debug.apk`
