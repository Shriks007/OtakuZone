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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.otakuzone.app.data.models.MangaDetails
import com.otakuzone.app.ui.theme.AccentPrimary
import com.otakuzone.app.ui.theme.DarkBackground
import com.otakuzone.app.ui.theme.DarkCardElevated
import com.otakuzone.app.ui.theme.DarkSurface
import com.otakuzone.app.ui.theme.TextMuted
import com.otakuzone.app.ui.theme.TextPrimary
import com.otakuzone.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun MangaDetailScreen(
    slug: String,
    onBackClick: () -> Unit,
    onChapterClick: (mangaSlug: String, chapterSlug: String, mangaTitle: String, chapterTitle: String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as OtakuZoneApplication
    val mangaRepo = app.mangaRepository
    val scope = rememberCoroutineScope()

    var details by remember { mutableStateOf<MangaDetails?>(null) }
    var isBookmarked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(slug) {
        isLoading = true
        val res = mangaRepo.getMangaDetails(slug)
        details = res.getOrNull()
        val bookmark = mangaRepo.getBookmark(slug)
        isBookmarked = bookmark != null
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentPrimary)
        }
        return
    }

    val item = details ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Failed to load manga details", color = TextMuted)
        }
        return
    }

    val chapters = item.chapters ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkCardElevated)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                if (isBookmarked) {
                                    mangaRepo.removeBookmark(slug)
                                    isBookmarked = false
                                } else {
                                    mangaRepo.saveReadingProgress(
                                        mangaSlug = slug,
                                        mangaTitle = item.title,
                                        cover = item.cover ?: "",
                                        chapterTitle = chapters.firstOrNull()?.title ?: "",
                                        chapterSlug = chapters.firstOrNull()?.slug ?: "",
                                        pageIndex = 0,
                                        totalChapters = chapters.size
                                    )
                                    isBookmarked = true
                                }
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkCardElevated)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) AccentPrimary else TextSecondary
                        )
                    }
                }
            }

            // Info Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    AsyncImage(
                        model = item.cover,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(110.dp, 160.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = item.title,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (!item.status.isNullOrBlank()) {
                            Text(text = "Status: ${item.status}", color = TextSecondary, fontSize = 12.sp)
                        }

                        if (!item.authors.isNullOrEmpty()) {
                            Text(
                                text = "Author: ${item.authors.joinToString()}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${chapters.size} Chapters",
                            color = AccentPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Synopsis
            if (!item.synopsis.isNullOrBlank() || !item.description.isNullOrBlank()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Synopsis",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = (item.synopsis ?: item.description ?: "").trim(),
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Chapter List
            item {
                Text(
                    text = "Chapters (${chapters.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            items(chapters) { chapter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCardElevated)
                        .clickable {
                            onChapterClick(slug, chapter.slug, item.title, chapter.title)
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chapter.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!chapter.uploaded.isNullOrBlank()) {
                        Text(
                            text = chapter.uploaded,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
