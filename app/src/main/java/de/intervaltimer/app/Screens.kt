package de.intervaltimer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------------------------
// Gemeinsame Bausteine
// ---------------------------------------------------------------------------------------------

/** Proportionaler Balken: rot = Übung, grün = Pause – zeigt auf einen Blick, wie ein Intervall aufgebaut ist. */
@Composable
fun SegmentBar(w: Workout, modifier: Modifier = Modifier, height: Int = 8) {
    Row(
        modifier.height(height.dp).clip(RoundedCornerShape(50)),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        w.segments.forEach { seg ->
            Box(
                Modifier.weight(seg.seconds.coerceAtLeast(1).toFloat()).fillMaxHeight()
                    .background(seg.type.accent()),
            )
        }
    }
}

@Composable
private fun Stat(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(16.dp), tint = Palette.TextDim)
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 14.sp, color = Palette.TextDim)
    }
}

// ---------------------------------------------------------------------------------------------
// Startseite
// ---------------------------------------------------------------------------------------------

@Composable
fun HomeScreen(
    workouts: List<Workout>,
    onStart: (Workout) -> Unit,
    onEdit: (Workout) -> Unit,
    onNew: () -> Unit,
    onDuplicate: (Workout) -> Unit,
    onDelete: (Workout) -> Unit,
) {
    var toDelete by remember { mutableStateOf<Workout?>(null) }

    Scaffold(
        containerColor = Palette.Bg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNew,
                containerColor = Palette.Accent,
                contentColor = Color.White,
                icon = { Icon(Ico.Add, null) },
                text = { Text("Neues Training", fontWeight = FontWeight.SemiBold) },
            )
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(Palette.Accent),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Ico.Timer, null, Modifier.size(28.dp), tint = Color.White) }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Intervall-Timer", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Tippe ein Training an, um zu starten.", fontSize = 14.sp, color = Palette.TextDim)
                    }
                }
            }
            items(workouts, key = { it.id }) { w ->
                var menu by remember { mutableStateOf(false) }
                Card(
                    onClick = { onStart(w) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Palette.Surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(52.dp).clip(CircleShape).background(Palette.Accent),
                                contentAlignment = Alignment.Center,
                            ) { Icon(Ico.Play, null, Modifier.size(30.dp), tint = Color.White) }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(w.name, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Stat(Ico.Repeat, if (w.sets > 1) "${w.sets} × ${w.intervals}" else "${w.intervals}×")
                                    Stat(Ico.Timer, formatTime(w.totalSeconds))
                                }
                            }
                            Box {
                                IconButton(onClick = { menu = true }) {
                                    Icon(Ico.More, "Menü", tint = Palette.TextDim)
                                }
                                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Bearbeiten") },
                                        leadingIcon = { Icon(Ico.Edit, null) },
                                        onClick = { menu = false; onEdit(w) },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Kopieren") },
                                        leadingIcon = { Icon(Ico.Copy, null) },
                                        onClick = { menu = false; onDuplicate(w) },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Löschen") },
                                        leadingIcon = { Icon(Ico.Delete, null) },
                                        onClick = { menu = false; toDelete = w },
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        SegmentBar(w, Modifier.padding(end = 8.dp))
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            w.segments.take(3).forEach { seg ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(seg.type.icon(), null, Modifier.size(14.dp), tint = seg.type.accent())
                                    Spacer(Modifier.width(4.dp))
                                    Text("${seg.displayName} ${seg.seconds}s", fontSize = 13.sp, color = Palette.TextDim)
                                }
                            }
                            if (w.segments.size > 3) Text("+${w.segments.size - 3}", fontSize = 13.sp, color = Palette.TextDim)
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { w ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            icon = { Icon(Ico.Delete, null) },
            title = { Text("Training löschen?") },
            text = { Text("„${w.name}“ wird entfernt.") },
            confirmButton = { TextButton(onClick = { onDelete(w); toDelete = null }) { Text("Löschen") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Abbrechen") } },
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Editor
// ---------------------------------------------------------------------------------------------

@Composable
private fun NumberField(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = Palette.TextDim,
    min: Int = 0,
    max: Int = 9999,
) {
    var text by remember { mutableStateOf(value.toString()) }
    LaunchedEffect(value) { if (text.toIntOrNull() != value) text = value.toString() }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(24.dp), tint = iconTint)
            Spacer(Modifier.width(10.dp))
        }
        FilledTonalIconButton(onClick = { onChange((value - 1).coerceAtLeast(min)) }, modifier = Modifier.size(44.dp)) {
            Icon(Ico.Remove, "weniger")
        }
        OutlinedTextField(
            value = text,
            onValueChange = { t ->
                val digits = t.filter { it.isDigit() }.take(4)
                text = digits
                onChange((digits.toIntOrNull() ?: min).coerceIn(min, max))
            },
            label = { Text(label, maxLines = 1) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
        )
        FilledTonalIconButton(onClick = { onChange((value + 1).coerceAtMost(max)) }, modifier = Modifier.size(44.dp)) {
            Icon(Ico.Add, "mehr")
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.Accent, letterSpacing = 1.sp)
}

@Composable
fun EditorScreen(initial: Workout, onSave: (Workout) -> Unit, onCancel: () -> Unit) {
    var w by remember { mutableStateOf(initial) }
    // „Einfach“ = genau eine Übung gefolgt von einer Pause – dann reichen zwei Zeitfelder.
    val simple = w.segments.size == 2 &&
        w.segments[0].type == SegmentType.WORK && w.segments[1].type == SegmentType.REST
    var advanced by remember { mutableStateOf(!simple || initial.sets > 1) }
    val valid = w.name.isNotBlank() && w.segments.isNotEmpty() && w.intervalSeconds > 0 &&
        w.intervals >= 1 && w.sets >= 1

    Scaffold(
        containerColor = Palette.Bg,
        bottomBar = {
            Column(Modifier.background(Palette.Surface)) {
                HorizontalDivider(color = Palette.Outline)
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegmentBar(w, height = 6)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Stat(Ico.Timer, "Intervall ${formatTime(w.intervalSeconds)}")
                        Stat(Ico.Repeat, "Gesamt ${formatTime(w.totalSeconds)}")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(48.dp)) {
                            Icon(Ico.Close, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Abbrechen")
                        }
                        Button(
                            onClick = { onSave(w) }, enabled = valid, modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.Accent, contentColor = Color.White),
                        ) {
                            Icon(Ico.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Speichern")
                        }
                    }
                }
            }
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Training", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            OutlinedTextField(
                value = w.name, onValueChange = { w = w.copy(name = it) },
                label = { Text("Name") }, singleLine = true, shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            if (simple) {
                NumberField(
                    "Übung (s)", w.segments[0].seconds,
                    { v -> w = w.copy(segments = w.segments.replaceAt(0, w.segments[0].copy(seconds = v))) },
                    icon = Ico.Dumbbell, iconTint = Palette.Work, min = 1,
                )
                NumberField(
                    "Pause (s)", w.segments[1].seconds,
                    { v -> w = w.copy(segments = w.segments.replaceAt(1, w.segments[1].copy(seconds = v))) },
                    icon = Ico.Moon, iconTint = Palette.Rest, min = 1,
                )
            }
            NumberField("Intervalle", w.intervals, { w = w.copy(intervals = it) }, icon = Ico.Repeat, min = 1, max = 999)

            TextButton(onClick = { advanced = !advanced }) {
                Text(if (advanced) "Erweiterte Optionen" else "Erweiterte Optionen", fontSize = 16.sp)
                Spacer(Modifier.width(4.dp))
                Icon(if (advanced) Ico.Up else Ico.Down, null)
            }

            if (advanced) {
                SectionTitle("ABLAUF")
                NumberField("Sätze", w.sets, { w = w.copy(sets = it) }, icon = Ico.Repeat, min = 1, max = 99)
                if (w.sets > 1) {
                    NumberField("Pause zwischen Sätzen (s)", w.setRestSeconds, { w = w.copy(setRestSeconds = it) }, icon = Ico.Moon)
                }
                NumberField("Vorbereitung (s)", w.prepSeconds, { w = w.copy(prepSeconds = it) }, icon = Ico.Timer)
                NumberField("Cooldown am Ende (s)", w.cooldownSeconds, { w = w.copy(cooldownSeconds = it) }, icon = Ico.Heart)
                NumberField(
                    "Countdown-Piepser (letzte s)", w.countdownSeconds, { w = w.copy(countdownSeconds = it) },
                    icon = Ico.VolumeOn, max = 30,
                )

                Spacer(Modifier.height(4.dp))
                SectionTitle("SEGMENTE PRO INTERVALL")
                Text(
                    "Lege mehrere Übungen und Pausen hintereinander. Ohne Namen heißen sie „Übung“ bzw. „Pause“.",
                    color = Palette.TextDim, fontSize = 13.sp,
                )
                w.segments.forEachIndexed { i, seg ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Palette.Surface),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.height(IntrinsicSize.Min)) {
                            Box(Modifier.width(6.dp).fillMaxHeight().background(seg.type.accent()))
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = seg.name,
                                        onValueChange = { v -> w = w.copy(segments = w.segments.replaceAt(i, seg.copy(name = v))) },
                                        label = { Text("Name (optional)", maxLines = 1) },
                                        placeholder = { Text(seg.displayName) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(onClick = { w = w.copy(segments = w.segments.moveBy(i, -1)) }, enabled = i > 0) {
                                        Icon(Ico.Up, "nach oben")
                                    }
                                    IconButton(onClick = { w = w.copy(segments = w.segments.moveBy(i, +1)) }, enabled = i < w.segments.lastIndex) {
                                        Icon(Ico.Down, "nach unten")
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = seg.type == SegmentType.WORK,
                                        onClick = { w = w.copy(segments = w.segments.replaceAt(i, seg.copy(type = SegmentType.WORK))) },
                                        label = { Text("Übung") },
                                        leadingIcon = { Icon(Ico.Dumbbell, null, Modifier.size(18.dp)) },
                                    )
                                    FilterChip(
                                        selected = seg.type == SegmentType.REST,
                                        onClick = { w = w.copy(segments = w.segments.replaceAt(i, seg.copy(type = SegmentType.REST))) },
                                        label = { Text("Pause") },
                                        leadingIcon = { Icon(Ico.Moon, null, Modifier.size(18.dp)) },
                                    )
                                    Spacer(Modifier.weight(1f))
                                    IconButton(
                                        onClick = { w = w.copy(segments = w.segments.filterIndexed { j, _ -> j != i }) },
                                        enabled = w.segments.size > 1,
                                    ) { Icon(Ico.Delete, "Segment löschen") }
                                }
                                NumberField(
                                    "Dauer (s)", seg.seconds,
                                    { v -> w = w.copy(segments = w.segments.replaceAt(i, seg.copy(seconds = v))) },
                                    icon = Ico.Timer, min = 1,
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = {
                        w = w.copy(segments = w.segments + Segment("", 30, SegmentType.WORK))
                    }) { Icon(Ico.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Übung") }
                    OutlinedButton(onClick = {
                        w = w.copy(segments = w.segments + Segment("", 10, SegmentType.REST))
                    }) { Icon(Ico.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Pause") }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun <T> List<T>.replaceAt(i: Int, v: T): List<T> = mapIndexed { j, x -> if (j == i) v else x }

private fun <T> List<T>.moveBy(i: Int, d: Int): List<T> {
    val j = i + d
    if (j !in indices) return this
    val m = toMutableList()
    val x = m.removeAt(i)
    m.add(j, x)
    return m
}
