package com.otakuzone.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Anikage.cc exact color palette ──────────────────────────────────────────
// Extracted directly from CSS variables on the site

// Backgrounds
val Background      = Color(0xFF0A0A0A)   // --bg: #0a0a0a
val Surface         = Color(0xFF101010)   // --surface: #101010
val SurfaceCard     = Color(0xFF191919)   // --surface-card: #191919
val SurfaceElevated = Color(0xFF1C1C1C)   // --surface-elevated: #1c1c1c
val SurfaceInput    = Color(0xFF2B2B2B)   // --surface-input: #2b2b2b

// Foreground
val Fg              = Color(0xFFF5F5F5)   // --fg: #f5f5f5
val FgMuted         = Color(0xFFA3A3A3)   // --fg-muted: #a3a3a3

// Accent (white-based for dark mode)
val AccentWhite     = Color(0xFFFFFFFF)
val AccentFg        = Color(0xFF101010)

// Accent scale (shades of gray/neutral, dark-mode)
val Accent50        = Color(0xFF0F0F0F)
val Accent100       = Color(0xFF191919)
val Accent200       = Color(0xFF242424)
val Accent300       = Color(0xFF333333)
val Accent400       = Color(0xFF4A4A4A)
val Accent500       = Color(0xFF737373)
val Accent600       = Color(0xFFA3A3A3)
val Accent700       = Color(0xFFC4C4C4)
val Accent800       = Color(0xFFE0E0E0)
val Accent900       = Color(0xFFF5F5F5)

// Semantic colors
val Danger          = Color(0xFFE53935)   // --danger (destructive)
val Warning         = Color(0xFFFBBF24)   // amber – used for star ratings
val Success         = Color(0xFF22C55E)   // green for airing badges
val Info            = Color(0xFF3B82F6)   // blue for info badges

// Play button
val PlayBg          = Color(0xFFFFFFFF)
val PlayFg          = Color(0xFF0A0A0A)

// Dividers / borders
val DividerColor    = Color(0xFF242424)
val BorderColor     = Color(0xFF333333)

// Shimmer colors for loading skeletons
val ShimmerBase     = Color(0xFF191919)
val ShimmerHighlight = Color(0xFF242424)

// Specific UI element colors
val EpisodeThumb    = Color(0xFF242424)
val CardHover       = Color(0x14FFFFFF)   // --surface-card-hover: #ffffff14
val FocusRing       = Color(0xFFBB86FC)   // purple focus ring (matches ring-focus oklch)

// Badge colors
val AiringBadge     = Color(0xFF16A34A)   // green for Airing
val FinishedBadge   = Color(0xFF525252)   // neutral for Finished
val PlanningBadge   = Color(0xFF1D4ED8)   // blue for Planning

// Score/rating star
val StarColor       = Color(0xFFFBBF24)

// Navigation
val NavSelected     = Color(0xFFFFFFFF)
val NavUnselected   = Color(0xFF737373)
val NavBackground   = Color(0xFF101010)

// Aliases mapped to Anikage palette for compatibility
val DarkBackground     = Background
val DarkSurface        = Surface
val DarkSurfaceVariant = SurfaceCard
val DarkCardSurface    = SurfaceCard
val DarkCardElevated   = SurfaceElevated

val AccentPrimary      = AccentWhite
val AccentPrimaryLight  = Accent800
val AccentPrimaryDark   = Accent600
val AccentSecondary    = Color(0xFFE11D48) // Anikage rose accent
val AccentSecondaryLight = Color(0xFFFB7185)

val AccentSuccess      = Success
val AccentWarning      = Warning
val AccentError        = Danger

val TextPrimary        = Fg
val TextSecondary      = FgMuted
val TextMuted          = Accent500

val SubBadgeColor      = Color(0xFF38BDF8) // Anikage sub badge cyan
val DubBadgeColor      = Color(0xFF4ADE80) // Anikage dub badge green
val ZokoBadgeColor     = Color(0xFFF43F5E) // Anikage default badge rose
