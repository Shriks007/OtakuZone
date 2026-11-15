package com.otakuzone.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.otakuzone.app.data.models.AniListMedia
import com.otakuzone.app.ui.theme.Background
import com.otakuzone.app.ui.theme.BorderColor
import com.otakuzone.app.ui.theme.DubBadgeColor
import com.otakuzone.app.ui.theme.Fg
import com.otakuzone.app.ui.theme.FgMuted
import com.otakuzone.app.ui.theme.StarColor
import com.otakuzone.app.ui.theme.SubBadgeColor
import com.otakuzone.app.ui.theme.SurfaceCard

/**
 * Anikage-styled anime poster card with exact badges (score, ep count, sub/dub).
 */
@Composable
fun AnimeCard(
    media: AniListMedia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Int = 148
) {
    Column(
        modifier = modifier
            .width(cardWidth.dp)
            .clickable { onClick() }
    ) {
        // Poster Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.70f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .background(SurfaceCard)
        ) {
            AsyncImage(
                model = media.getBestCover(),
                contentDescription = media.getDisplayName(),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top-left: Airing / Status badge
            if (media.status == "RELEASING") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF16A34A).copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AIRING",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Top-right: Score badge (e.g. 8.5)
            if (media.averageScore != null && media.averageScore > 0) {
                val score = String.format("%.1f", media.averageScore / 10.0)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC0A0A0A))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StarColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = score,
                            color = Fg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom gradient overlay for sub/dub chips
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xE60A0A0A))
                        )
                    )
            )

            // Bottom tags: Sub / Dub / Total Ep counts
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val eps = media.nextAiringEpisode?.episode?.minus(1)
                    ?: media.episodes
                    ?: 0

                if (eps > 0) {
                    // SUB badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1F2937))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ClosedCaption,
                                contentDescription = "Sub",
                                tint = SubBadgeColor,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$eps",
                                color = Fg,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Total episodes badge if known
                    if (media.episodes != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF111827))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${media.episodes}",
                                color = FgMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = media.getDisplayName(),
            color = Fg,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Format & Status Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (media.status == "RELEASING") {
                Text(
                    text = "Airing",
                    color = Color(0xFF22C55E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(text = "·", color = FgMuted, fontSize = 11.sp)
            }
            Text(
                text = media.format ?: "TV",
                color = FgMuted,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Anikage Top Anime Ranked row item (Rank 1, 2, 3...)
 */
@Composable
fun TopAnimeRankItem(
    rank: Int,
    media: AniListMedia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(SurfaceCard)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank pill
        val rankBg = when (rank) {
            1 -> Color(0xFFEAB308) // Gold
            2 -> Color(0xFF94A3B8) // Silver
            3 -> Color(0xFFD97706) // Bronze
            else -> Color(0xFF262626)
        }
        val rankTextColor = if (rank <= 3) Color(0xFF0A0A0A) else Fg

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(rankBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                color = rankTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Poster thumb
        AsyncImage(
            model = media.getBestCover(),
            contentDescription = media.getDisplayName(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(42.dp)
                .height(58.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Background)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = media.getDisplayName(),
                color = Fg,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (media.averageScore != null && media.averageScore > 0) {
                    val score = String.format("%.1f", media.averageScore / 10.0)
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(text = score, color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "·", color = FgMuted, fontSize = 11.sp)
                }

                Text(
                    text = media.format ?: "TV",
                    color = FgMuted,
                    fontSize = 11.sp
                )

                if (media.status == "RELEASING") {
                    Text(text = "·", color = FgMuted, fontSize = 11.sp)
                    Text(
                        text = "Airing",
                        color = Color(0xFF22C55E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
