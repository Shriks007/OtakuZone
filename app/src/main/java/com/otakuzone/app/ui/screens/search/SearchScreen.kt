package com.otakuzone.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.otakuzone.app.OtakuZoneApplication
import com.otakuzone.app.data.models.AniListMedia
import com.otakuzone.app.data.models.MangaItem
import com.otakuzone.app.ui.components.AnimeCard
import com.otakuzone.app.ui.theme.AccentWhite
import com.otakuzone.app.ui.theme.Background
import com.otakuzone.app.ui.theme.BorderColor
import com.otakuzone.app.ui.theme.Fg
import com.otakuzone.app.ui.theme.FgMuted
import com.otakuzone.app.ui.theme.Surface
import com.otakuzone.app.ui.theme.SurfaceCard
import com.otakuzone.app.ui.theme.SurfaceInput
import kotlinx.coroutines.launch

enum class SearchMode {
    ANIME,
    MANGA
}

@Composable
fun SearchScreen(
    onAnimeClick: (Int) -> Unit,
    onMangaClick: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as OtakuZoneApplication
    val animeRepo = app.animeRepository
    val mangaRepo = app.mangaRepository
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(SearchMode.ANIME) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var animeResults by remember { mutableStateOf<List<AniListMedia>>(emptyList()) }
    var mangaResults by remember { mutableStateOf<List<MangaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val genres = listOf(
        "Action", "Adventure", "Comedy", "Drama", "Fantasy",
        "Romance", "Sci-Fi", "Slice of Life", "Supernatural", "Mystery"
    )

    fun performSearch() {
        if (query.isBlank() && selectedGenre == null) return
        scope.launch {
            isLoading = true
            if (searchMode == SearchMode.ANIME) {
                val searchQuery = query.ifBlank { selectedGenre ?: "" }
                animeResults = animeRepo.searchAnime(searchQuery).getOrNull() ?: emptyList()
            } else {
                mangaResults = mangaRepo.searchManga(query).getOrNull() ?: emptyList()
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Title
        Text(
            text = "Browse",
            color = Fg,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input (Anikage style)
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (it.length >= 3) {
                    performSearch()
                }
            },
            placeholder = { Text("Search title, character, or studio...", color = FgMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = FgMuted, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = ""; animeResults = emptyList(); mangaResults = emptyList() }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = FgMuted, modifier = Modifier.size(18.dp))
                    }
                }
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

        // Search Mode Tabs & Genre Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(SearchMode.ANIME to "Anime", SearchMode.MANGA to "Manga").forEach { (mode, title) ->
                val isSelected = searchMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) AccentWhite else SurfaceCard)
                        .border(1.dp, if (isSelected) AccentWhite else BorderColor, RoundedCornerShape(6.dp))
                        .clickable {
                            searchMode = mode
                            performSearch()
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color.Black else Fg,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (searchMode == SearchMode.ANIME) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(genres) { genre ->
                    val isSelected = selectedGenre == genre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFF262626) else SurfaceCard)
                            .border(1.dp, if (isSelected) AccentWhite else BorderColor, RoundedCornerShape(4.dp))
                            .clickable {
                                selectedGenre = if (isSelected) null else genre
                                performSearch()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = genre,
                            color = if (isSelected) Fg else FgMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = AccentWhite,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else if (searchMode == SearchMode.ANIME) {
            if (animeResults.isEmpty() && query.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No anime found for \"$query\"", color = FgMuted, fontSize = 14.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 135.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(animeResults) { media ->
                        AnimeCard(
                            media = media,
                            onClick = { onAnimeClick(media.id) },
                            cardWidth = 140
                        )
                    }
                }
            }
        } else {
            if (mangaResults.isEmpty() && query.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No manga found for \"$query\"", color = FgMuted, fontSize = 14.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 135.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(mangaResults) { manga ->
                        AnikageMangaSearchCard(
                            manga = manga,
                            onClick = { onMangaClick(manga.slug) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnikageMangaSearchCard(
    manga: MangaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.70f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .background(SurfaceCard)
        ) {
            AsyncImage(
                model = manga.cover,
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (!manga.latestChapter.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = manga.latestChapter,
                        color = Fg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = manga.title,
            color = Fg,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
