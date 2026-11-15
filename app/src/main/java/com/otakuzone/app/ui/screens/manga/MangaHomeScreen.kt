package com.otakuzone.app.ui.screens.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.otakuzone.app.OtakuZoneApplication
import com.otakuzone.app.data.local.MangaBookmarkEntity
import com.otakuzone.app.data.models.MangaItem
import com.otakuzone.app.ui.theme.AccentPrimary
import com.otakuzone.app.ui.theme.DarkBackground
import com.otakuzone.app.ui.theme.DarkCardElevated
import com.otakuzone.app.ui.theme.DarkSurface
import com.otakuzone.app.ui.theme.TextMuted
import com.otakuzone.app.ui.theme.TextPrimary
import com.otakuzone.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun MangaHomeScreen(
    onMangaClick: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as OtakuZoneApplication
    val mangaRepo = app.mangaRepository
    val scope = rememberCoroutineScope()

    var mangaList by remember { mutableStateOf<List<MangaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val bookmarks by mangaRepo.getBookmarksFlow().collectAsState(initial = emptyList())

    fun loadManga() {
        scope.launch {
            isLoading = true
            val res = mangaRepo.getMangaHome()
            mangaList = res.getOrNull() ?: emptyList()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadManga()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manga Vault 📖",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { loadManga() }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary)
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPrimary)
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Bookmarks / Reading History
            if (bookmarks.isNotEmpty()) {
                item {
                    Text(
                        text = "Your Bookmarked Manga 🔖",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 10.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookmarks) { bookmark ->
                            MangaBookmarkCard(
                                bookmark = bookmark,
                                onClick = { onMangaClick(bookmark.slug) }
                            )
                        }
                    }
                }
            }

            // Popular / Trending Manga
            item {
                Text(
                    text = "Trending Manga (Manganato) 🔥",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 10.dp)
                )

                if (mangaList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ensure MangaVault backend is running (see Settings to configure URL)",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mangaList) { manga ->
                            MangaCard(
                                manga = manga,
                                onClick = { onMangaClick(manga.slug) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MangaCard(
    manga: MangaItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
    ) {
        Column {
            AsyncImage(
                model = manga.cover,
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = manga.title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!manga.latestChapter.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = manga.latestChapter,
                        color = AccentPrimary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MangaBookmarkCard(
    bookmark: MangaBookmarkEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
    ) {
        Column {
            AsyncImage(
                model = bookmark.cover,
                contentDescription = bookmark.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = bookmark.title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!bookmark.lastReadChapterTitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = bookmark.lastReadChapterTitle,
                        color = AccentPrimary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
