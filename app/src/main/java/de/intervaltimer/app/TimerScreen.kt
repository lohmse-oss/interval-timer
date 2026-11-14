package de.intervaltimer.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun CircleButton(
    size: Int,
    bg: Color,
    description: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(bg)
            .semantics { contentDescription = description }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun Pill(icon: ImageVector, text: String) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = Color.White)
        Spacer(Modifier.width(5.dp))
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
    }
}

@Composable
private fun SmallAction(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, Modifier.size(26.dp), tint = Color.White.copy(alpha = if (active) 1f else 0.45f))
        Text(label, color = Color.White.copy(alpha = if (active) 0.95f else 0.55f), fontSize = 12.sp)
    }
}

@Composable
fun TimerScreen(state: RunState, storage: Storage) {
    val context = LocalContext.current
    KeepScreenOn()

    var confirmStop by remember { mutableStateOf(false) }
    BackHandler { if (state.finished) TimerHolder.set(null) else confirmStop = true }

    var sound by remember { mutableStateOf(storage.soundOn) }
    var vibration by remember { mutableStateOf(storage.vibrationOn) }

    val flash = remember { Animatable(0f) }
    LaunchedEffect(state.changeCount) {
        if (state.changeCount > 0) {
            flash.snapTo(0.9f)
            flash.animateTo(0f, tween(500))
        }
    }

    val phase = state.phase
    val w = state.workout
    val (top, bottom) = if (state.finished) Color(0xFF334155) to Color(0xFF0F172A) else phase.kind.gradient()
    val totalMs = remember(state.phases) { state.phases.sumOf { it.seconds * 1000L }.coerceAtLeast(1) }
    val secs = ((state.remainingMs + 999) / 1000).toInt()

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.finished) {
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(140.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Ico.Check, null, Modifier.size(84.dp), tint = Color.White) }
                Spacer(Modifier.height(24.dp))
                Text("Geschafft!", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                Text(w.name, color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (w.sets > 1) Pill(Ico.Repeat, "${w.sets} Sätze")
                    Pill(Ico.Dumbbell, "${w.sets * w.intervals} Intervalle")
                    Pill(Ico.Timer, formatTime(w.totalSeconds))
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { TimerHolder.set(null) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                ) { Text("Fertig", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            } else {
                // Gesamtfortschritt
                val progress = (1f - state.totalRemainingMs / totalMs.toFloat()).coerceIn(0f, 1f)
                Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.2f))) {
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color.White))
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (w.sets > 1 && phase.set > 0) Pill(Ico.Repeat, "Satz ${phase.set}/${w.sets}")
                    if (phase.interval > 0) Pill(Ico.Dumbbell, "Int. ${phase.interval}/${w.intervals}")
                    Pill(Ico.Timer, formatTime(((state.totalRemainingMs + 999) / 1000).toInt()))
                }

                Spacer(Modifier.weight(1f))
                Icon(phase.kind.icon(), null, Modifier.size(52.dp), tint = Color.White)
                Spacer(Modifier.height(6.dp))
                Text(
                    phase.label.uppercase(),
                    color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center, maxLines = 2,
                )
                Spacer(Modifier.height(16.dp))

                // Fortschrittsring mit Restzeit
                val frac = (state.remainingMs / (phase.seconds * 1000f)).coerceIn(0f, 1f)
                Box(Modifier.size(290.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 18.dp.toPx()
                        val topLeft = Offset(stroke / 2, stroke / 2)
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        drawArc(
                            Color.White.copy(alpha = 0.2f), 0f, 360f, false,
                            topLeft = topLeft, size = arcSize, style = Stroke(stroke),
                        )
                        if (frac > 0f) {
                            drawArc(
                                Color.White, -90f, 360f * frac, false,
                                topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (secs >= 60) formatTime(secs) else secs.toString(),
                            color = Color.White,
                            style = TextStyle(fontSize = 104.sp, fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum"),
                        )
                        if (state.paused) {
                            Text("PAUSIERT", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))

                // Vorschau auf das nächste Segment
                Row(
                    Modifier.clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.22f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val nx = state.next
                    if (nx != null) {
                        Text("Danach", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Spacer(Modifier.width(10.dp))
                        Icon(nx.kind.icon(), null, Modifier.size(20.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("${nx.label} · ${nx.seconds}s", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Ico.Check, null, Modifier.size(20.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Letzter Abschnitt", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircleButton(60, Color.White.copy(alpha = 0.2f), "Zurück", { TimerService.command(context, TimerService.ACTION_PREV) }) {
                        Icon(Ico.SkipPrev, null, Modifier.size(30.dp), tint = Color.White)
                    }
                    CircleButton(
                        92, Color.White, if (state.paused) "Weiter" else "Pause",
                        { TimerService.command(context, TimerService.ACTION_TOGGLE_PAUSE) },
                    ) {
                        Icon(if (state.paused) Ico.Play else Ico.Pause, null, Modifier.size(52.dp), tint = bottom)
                    }
                    CircleButton(60, Color.White.copy(alpha = 0.2f), "Weiter zum nächsten", { TimerService.command(context, TimerService.ACTION_NEXT) }) {
                        Icon(Ico.SkipNext, null, Modifier.size(30.dp), tint = Color.White)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SmallAction(if (sound) Ico.VolumeOn else Ico.VolumeOff, "Ton", sound) {
                        sound = !sound; storage.soundOn = sound
                    }
                    SmallAction(Ico.Vibration, "Vibration", vibration) {
                        vibration = !vibration; storage.vibrationOn = vibration
                    }
                    SmallAction(Ico.Stop, "Beenden", true) { confirmStop = true }
                }
            }
        }

        // Weißer Blitz bei jedem Segmentwechsel
        Box(Modifier.fillMaxSize().alpha(flash.value).background(Color.White))
    }

    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
            icon = { Icon(Ico.Stop, null) },
            title = { Text("Training beenden?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmStop = false
                    TimerService.command(context, TimerService.ACTION_STOP)
                }) { Text("Beenden") }
            },
            dismissButton = { TextButton(onClick = { confirmStop = false }) { Text("Weiter trainieren") } },
        )
    }
}
