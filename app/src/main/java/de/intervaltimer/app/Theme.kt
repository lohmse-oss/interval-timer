package de.intervaltimer.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Palette {
    val Bg = Color(0xFF0E1014)
    val Surface = Color(0xFF181B22)
    val SurfaceHigh = Color(0xFF222630)
    val Outline = Color(0xFF343A47)
    val Text = Color(0xFFF1F3F7)
    val TextDim = Color(0xFF9AA3B2)
    val Accent = Color(0xFFFF6B3D)

    val Work = Color(0xFFEF4444)
    val Rest = Color(0xFF22C55E)
}

private val scheme = darkColorScheme(
    primary = Palette.Accent,
    onPrimary = Color.White,
    secondary = Palette.Accent,
    secondaryContainer = Color(0xFF45281F),
    onSecondaryContainer = Palette.Text,
    background = Palette.Bg,
    onBackground = Palette.Text,
    surface = Palette.Surface,
    onSurface = Palette.Text,
    surfaceVariant = Palette.SurfaceHigh,
    onSurfaceVariant = Palette.TextDim,
    surfaceContainerLowest = Palette.Bg,
    surfaceContainerLow = Palette.Surface,
    surfaceContainer = Palette.Surface,
    surfaceContainerHigh = Palette.SurfaceHigh,
    surfaceContainerHighest = Palette.SurfaceHigh,
    outline = Palette.Outline,
    outlineVariant = Palette.Outline,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}

/** Verlauf (oben, unten) für den Timer-Hintergrund je Phase. */
fun PhaseKind.gradient(): Pair<Color, Color> = when (this) {
    PhaseKind.WORK -> Color(0xFFE53935) to Color(0xFF7F1D1D)
    PhaseKind.REST -> Color(0xFF2EAD62) to Color(0xFF14532D)
    PhaseKind.PREP -> Color(0xFF2F6BF0) to Color(0xFF1E3A8A)
    PhaseKind.SET_REST -> Color(0xFFF59E0B) to Color(0xFF92400E)
    PhaseKind.COOLDOWN -> Color(0xFF14B8A6) to Color(0xFF115E59)
}

fun SegmentType.accent(): Color = if (this == SegmentType.WORK) Palette.Work else Palette.Rest
