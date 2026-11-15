package com.otakuzone.app.ui.screens.search

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.otakuzone.app.ui.theme.AccentPrimary
import com.otakuzone.app.ui.theme.DarkBackground
import com.otakuzone.app.ui.theme.DarkCardElevated
import com.otakuzone.app.ui.theme.DarkSurface
import com.otakuzone.app.ui.theme.TextMuted
import com.otakuzone.app.ui.theme.TextPrimary
import com.otakuzone.app.ui.theme.TextSecondary
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
    var animeResults by remember { mutableStateOf<List<AniListMedia>>(emptyList()) }
    var mangaResults by remember { mutableStateOf<List<MangaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    fun performSearch() {
        if (query.isBlank()) return
        scope.launch {
            isLoading = true
            if (searchMode == SearchMode.ANIME) {
                animeResults = animeRepo.searchAnime(query).getOrNull() ?: emptyList()
            } else {
                mangaResults = mangaRepo.searchManga(query).getOrNull() ?: emptyList()
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Discover",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (it.length >= 3) {
                    performSearch()
                }
            },
            placeholder = { Text("Search Anime or Manga title...", color = TextMuted) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = ""; animeResults = emptyList(); mangaResults = emptyList() }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
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

        // Search Mode Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = searchMode == SearchMode.ANIME,
                onClick = {
                    searchMode = SearchMode.ANIME
                    performSearch()
                },
                label = { Text("Anime") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentPrimary.copy(alpha = 0.25f),
                    selectedLabelColor = AccentPrimary,
                    containerColor = DarkSurface,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = searchMode == SearchMode.MANGA,
                onClick = {
                    searchMode = SearchMode.MANGA
                    performSearch()
                },
                label = { Text("Manga") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentPrimary.copy(alpha = 0.25f),
                    selectedLabelColor = AccentPrimary,
                    containerColor = DarkSurface,
                    labelColor = TextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPrimary)
            }
        } else if (searchMode == SearchMode.ANIME) {
            if (animeResults.isEmpty() && query.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No anime found for \"$query\"", color = TextMuted)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(animeResults) { media ->
                        AnimeCard(
                            media = media,
                            onClick = { onAnimeClick(media.id) },
                            cardWidth = 110,
                            cardHeight = 160
                        )
                    }
                }
            }
        } else {
            if (mangaResults.isEmpty() && query.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No manga found for \"$query\"", color = TextMuted)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(mangaResults) { manga ->
                        MangaSearchCard(
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
fun MangaSearchCard(
    manga: MangaItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
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
