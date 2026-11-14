package de.intervaltimer.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Eigene Piktogramme (24×24-Pfade), damit keine große Icon-Bibliothek nötig ist. */
object Ico {
    val Play by lazy { make("Play", "M8,5v14l11,-7z") }
    val Pause by lazy { make("Pause", "M6,19h4V5H6v14zM14,5v14h4V5h-4z") }
    val SkipNext by lazy { make("SkipNext", "M6,18l8.5,-6L6,6v12zM16,6v12h2V6h-2z") }
    val SkipPrev by lazy { make("SkipPrev", "M6,6h2v12H6zM9.5,12l8.5,6V6z") }
    val Stop by lazy { make("Stop", "M6,6h12v12H6z") }
    val Add by lazy { make("Add", "M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z") }
    val Remove by lazy { make("Remove", "M19,13H5v-2h14v2z") }
    val Close by lazy {
        make("Close", "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z")
    }
    val Check by lazy { make("Check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z") }
    val Delete by lazy {
        make("Delete", "M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z")
    }
    val Edit by lazy {
        make(
            "Edit",
            "M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z",
        )
    }
    val Copy by lazy {
        make(
            "Copy",
            "M16,1H4C2.9,1 2,1.9 2,3v14h2V3h12V1zM19,5H8C6.9,5 6,5.9 6,7v14c0,1.1 0.9,2 2,2h11c1.1,0 2,-0.9 2,-2V7C21,5.9 20.1,5 19,5zM19,21H8V7h11V21z",
        )
    }
    val More by lazy {
        make(
            "More",
            "M12,8c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM12,16c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z",
        )
    }
    val Up by lazy { make("Up", "M7.41,15.41L12,10.83l4.59,4.58L18,14l-6,-6 -6,6z") }
    val Down by lazy { make("Down", "M7.41,8.59L12,13.17l4.59,-4.58L18,10l-6,6 -6,-6z") }
    val Repeat by lazy {
        make("Repeat", "M7,7h10v3l4,-4 -4,-4v3H5v6h2V7zM17,17H7v-3l-4,4 4,4v-3h12v-6h-2v4z")
    }
    val Timer by lazy {
        make(
            "Timer",
            "M15,1H9v2h6V1zM11,14h2V8h-2v6zM19.03,7.39l1.42,-1.42c-0.43,-0.51 -0.9,-0.99 -1.41,-1.41l-1.42,1.42C16.07,4.74 14.12,4 12,4c-4.97,0 -9,4.03 -9,9s4.02,9 9,9 9,-4.03 9,-9c0,-2.12 -0.74,-4.07 -1.97,-5.61zM12,20c-3.87,0 -7,-3.13 -7,-7s3.13,-7 7,-7 7,3.13 7,7 -3.13,7 -7,7z",
        )
    }
    val Dumbbell by lazy {
        make(
            "Dumbbell",
            "M20.57,14.86L22,13.43 20.57,12 17,15.57 8.43,7 12,3.43 10.57,2 9.14,3.43 7.71,2 5.57,4.14 4.14,2.71 2.71,4.14l1.43,1.43L2,7.71l1.43,1.43L2,10.57 3.43,12 7,8.43 15.57,17 12,20.57 13.43,22l1.43,-1.43L16.29,22l2.14,-2.14 1.43,1.43 1.43,-1.43 -1.43,-1.43L22,16.29z",
        )
    }
    val Moon by lazy {
        make(
            "Moon",
            "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9 9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36 -0.98,1.37 -2.58,2.26 -4.4,2.26 -2.98,0 -5.4,-2.42 -5.4,-5.4 0,-1.81 0.89,-3.42 2.26,-4.4C12.92,3.04 12.46,3 12,3z",
        )
    }
    val Heart by lazy {
        make(
            "Heart",
            "M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z",
        )
    }
    val VolumeOn by lazy {
        make(
            "VolumeOn",
            "M3,9v6h4l5,5V4L7,9H3zM16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v8.05c1.48,-0.73 2.5,-2.25 2.5,-4.02zM14,3.23v2.06c2.89,0.86 5,3.54 5,6.71s-2.11,5.85 -5,6.71v2.06c4.01,-0.91 7,-4.49 7,-8.77s-2.99,-7.86 -7,-8.77z",
        )
    }
    val VolumeOff by lazy {
        make(
            "VolumeOff",
            "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63zM19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71zM4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3zM12,4L9.91,6.09 12,8.18V4z",
        )
    }
    val Back by lazy { make("Back", "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z") }
    val History by lazy {
        make(
            "History",
            "M13,3c-4.97,0 -9,4.03 -9,9H1l3.89,3.89 0.07,0.14L9,12H6c0,-3.87 3.13,-7 7,-7s7,3.13 7,7 -3.13,7 -7,7c-1.93,0 -3.68,-0.79 -4.94,-2.06l-1.42,1.42C8.27,19.99 10.51,21 13,21c4.97,0 9,-4.03 9,-9s-4.03,-9 -9,-9zM12,8v5l4.28,2.54 0.72,-1.21 -3.5,-2.08V8H12z",
        )
    }
    val Flame by lazy {
        make(
            "Flame",
            "M19.48,12.35c-1.57,-4.08 -7.16,-4.3 -5.81,-10.23 0.1,-0.44 -0.37,-0.78 -0.75,-0.55C9.29,3.71 6.68,8 8.87,13.62c0.18,0.46 -0.36,0.89 -0.75,0.59 -1.81,-1.37 -2,-3.34 -1.84,-4.75 0.06,-0.52 -0.62,-0.77 -0.91,-0.34C4.69,10.16 4,11.84 4,14.37c0.38,5.6 5.11,7.32 6.81,7.54 2.43,0.31 5.06,-0.14 6.95,-1.87 2.08,-1.93 2.84,-5.01 1.72,-7.69zM10.2,17.38c1.44,-0.35 2.18,-1.39 2.38,-2.31 0.33,-1.43 -0.96,-2.83 -0.09,-5.09 0.33,1.87 3.27,3.04 3.27,5.08 0.08,2.53 -2.66,4.7 -5.56,2.32z",
        )
    }
    val Vibration by lazy {
        make(
            "Vibration",
            "M0,15h2V9H0v6zM3,17h2V7H3v10zM22,9v6h2V9h-2zM19,17h2V7h-2v10zM16.5,3h-9C6.67,3 6,3.67 6,4.5v15c0,0.83 0.67,1.5 1.5,1.5h9c0.83,0 1.5,-0.67 1.5,-1.5v-15c0,-0.83 -0.67,-1.5 -1.5,-1.5zM16,19H8V5h8v14z",
        )
    }

    private fun make(name: String, path: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
            .addPath(PathParser().parsePathString(path).toNodes(), fill = SolidColor(Color.Black))
            .build()
}

fun PhaseKind.icon(): ImageVector = when (this) {
    PhaseKind.WORK -> Ico.Dumbbell
    PhaseKind.REST, PhaseKind.SET_REST -> Ico.Moon
    PhaseKind.PREP -> Ico.Timer
    PhaseKind.COOLDOWN -> Ico.Heart
}

fun SegmentType.icon(): ImageVector = if (this == SegmentType.WORK) Ico.Dumbbell else Ico.Moon
