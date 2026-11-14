package de.intervaltimer.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private val AbortOrange = Color(0xFFF59E0B)

private fun HistoryEntry.date(): LocalDate =
    Instant.ofEpochMilli(endedAt).atZone(ZoneId.systemDefault()).toLocalDate()

private fun HistoryEntry.time(): LocalTime =
    Instant.ofEpochMilli(endedAt).atZone(ZoneId.systemDefault()).toLocalTime()

/** Anzahl aufeinanderfolgender Tage mit mindestens einem fertigen Training (heute oder gestern als Ende). */
fun computeStreak(entries: List<HistoryEntry>, today: LocalDate = LocalDate.now()): Int {
    val days = entries.filter { it.completed }.map { it.date() }.toSet()
    var day = if (today in days) today else today.minusDays(1)
    var n = 0
    while (day in days) { n++; day = day.minusDays(1) }
    return n
}

private fun minutesText(seconds: Int): String =
    if (seconds < 60) "$seconds s" else "${(seconds + 30) / 60} Min"

@Composable
private fun StatCard(icon: ImageVector, tint: Color, value: String, label: String, modifier: Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Palette.Surface),
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, Modifier.size(22.dp), tint = tint)
            Text(value, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, softWrap = false)
            Text(label, fontSize = 12.sp, color = Palette.TextDim, maxLines = 2)
        }
    }
}

@Composable
private fun WeekChart(entries: List<HistoryEntry>) {
    val today = LocalDate.now()
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val minutes = days.map { d -> entries.filter { it.date() == d }.sumOf { it.trainedSeconds } / 60f }
    val max = (minutes.maxOrNull() ?: 0f).coerceAtLeast(10f)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Palette.Surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Letzte 7 Tage (Minuten)", fontSize = 13.sp, color = Palette.TextDim)
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                days.forEachIndexed { i, d ->
                    val m = minutes[i]
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (m >= 1f) m.toInt().toString() else "", fontSize = 11.sp, color = Palette.TextDim,
                            maxLines = 1,
                        )
                        Spacer(Modifier.height(2.dp))
                        Box(
                            Modifier.width(22.dp).height((m / max * 64f).coerceAtLeast(4f).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (m > 0f) Palette.Accent else Palette.SurfaceHigh),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.GERMAN).take(2),
                            fontSize = 12.sp,
                            color = if (d == today) Palette.Text else Palette.TextDim,
                            fontWeight = if (d == today) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    storage: Storage,
    existingWorkoutIds: Set<Long>,
    onBack: () -> Unit,
    onRepeat: (Long) -> Unit,
) {
    BackHandler(onBack = onBack)
    var entries by remember { mutableStateOf(storage.loadHistory()) }
    var confirmClear by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val last7 = entries.filter { it.date() >= today.minusDays(6) }
    val doneWeek = last7.count { it.completed }
    val weekSeconds = last7.sumOf { it.trainedSeconds }
    val streak = computeStreak(entries)

    Scaffold(containerColor = Palette.Bg) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Ico.Back, "Zurück") }
                    Text("Verlauf", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    if (entries.isNotEmpty()) {
                        IconButton(onClick = { confirmClear = true }) { Icon(Ico.Delete, "Alles löschen", tint = Palette.TextDim) }
                    }
                }
            }
            if (entries.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(96.dp).clip(CircleShape).background(Palette.Surface),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Ico.History, null, Modifier.size(48.dp), tint = Palette.TextDim) }
                        Spacer(Modifier.height(16.dp))
                        Text("Noch keine Trainings", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Sobald du ein Training abschließt, erscheint es hier.",
                            color = Palette.TextDim, fontSize = 14.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            } else {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(Ico.Check, Palette.Rest, doneWeek.toString(), "Trainings\n7 Tage", Modifier.weight(1f))
                        StatCard(Ico.Timer, Palette.Accent, minutesText(weekSeconds), "Zeit\n7 Tage", Modifier.weight(1f))
                        StatCard(
                            Ico.Flame, AbortOrange,
                            if (streak == 1) "1 Tag" else "$streak Tage", "Serie\nin Folge", Modifier.weight(1f),
                        )
                    }
                }
                item { WeekChart(entries) }

                val groups = entries.groupBy { it.date() }.toList().sortedByDescending { it.first }
                groups.forEach { (day, list) ->
                    item(key = "h$day") {
                        Text(
                            when (day) {
                                today -> "Heute"
                                today.minusDays(1) -> "Gestern"
                                else -> day.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMAN))
                            },
                            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.Accent,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(list, key = { it.endedAt }) { e ->
                        HistoryRow(
                            e,
                            canRepeat = e.workoutId in existingWorkoutIds,
                            onRepeat = { onRepeat(e.workoutId) },
                            onDelete = { storage.deleteHistory(e.endedAt); entries = storage.loadHistory() },
                        )
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            icon = { Icon(Ico.Delete, null) },
            title = { Text("Gesamten Verlauf löschen?") },
            text = { Text("Alle ${entries.size} Einträge werden entfernt.") },
            confirmButton = {
                TextButton(onClick = { storage.clearHistory(); entries = emptyList(); confirmClear = false }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Abbrechen") } },
        )
    }
}

@Composable
private fun HistoryRow(e: HistoryEntry, canRepeat: Boolean, onRepeat: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val tint = if (e.completed) Palette.Rest else AbortOrange
    Card(
        onClick = { if (canRepeat) onRepeat() },
        enabled = canRepeat,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Palette.Surface, disabledContainerColor = Palette.Surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) { Icon(if (e.completed) Ico.Check else Ico.Stop, null, Modifier.size(22.dp), tint = tint) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(e.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(
                    buildString {
                        append(e.time().format(DateTimeFormatter.ofPattern("HH:mm")))
                        append(" · ")
                        append(minutesText(e.trainedSeconds))
                        if (!e.completed) {
                            append(" · Abbruch bei ${e.progressPercent} %")
                            if (e.set > 0 && e.interval > 0) {
                                append(" (")
                                if (e.sets > 1) append("Satz ${e.set}/${e.sets}, ")
                                append("Int. ${e.interval}/${e.intervals})")
                            }
                        }
                    },
                    fontSize = 13.sp, color = Palette.TextDim,
                )
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Ico.More, "Menü", tint = Palette.TextDim) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Löschen") },
                        leadingIcon = { Icon(Ico.Delete, null) },
                        onClick = { menu = false; onDelete() },
                    )
                }
            }
        }
    }
}
