package de.intervaltimer.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------------------------
// Farben je Phase
// ---------------------------------------------------------------------------------------------

private val RedWork = Color(0xFFD32F2F)
private val GreenRest = Color(0xFF2E7D32)
private val BluePrep = Color(0xFF1565C0)
private val OrangeSetRest = Color(0xFFEF6C00)
private val TealCooldown = Color(0xFF00796B)

private fun PhaseKind.color(): Color = when (this) {
    PhaseKind.WORK -> RedWork
    PhaseKind.REST -> GreenRest
    PhaseKind.PREP -> BluePrep
    PhaseKind.SET_REST -> OrangeSetRest
    PhaseKind.COOLDOWN -> TealCooldown
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
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onNew) { Text("+ Neues Training") }
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Intervall-Timer", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Wähle ein Training oder erstelle ein neues.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(workouts, key = { it.id }) { w ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(w.name, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            buildString {
                                if (w.sets > 1) append("${w.sets} Sätze × ")
                                append("${w.intervals} Intervalle")
                                append(" · Intervall ${formatTime(w.intervalSeconds)}")
                            },
                        )
                        Text(
                            "Gesamt ${formatTime(w.totalSeconds)}  ·  " +
                                w.segments.joinToString(" / ") { "${it.name} ${it.seconds}s" },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = { onStart(w) }) { Text("Start") }
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { onEdit(w) }) { Text("Bearbeiten") }
                            TextButton(onClick = { onDuplicate(w) }) { Text("Kopie") }
                            TextButton(onClick = { toDelete = w }) { Text("Löschen") }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { w ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
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
    min: Int = 0,
    max: Int = 9999,
) {
    var text by remember { mutableStateOf(value.toString()) }
    LaunchedEffect(value) { if (text.toIntOrNull() != value) text = value.toString() }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { onChange((value - 1).coerceAtLeast(min)) },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(44.dp),
        ) { Text("−", fontSize = 20.sp) }
        OutlinedTextField(
            value = text,
            onValueChange = { t ->
                val digits = t.filter { it.isDigit() }.take(4)
                text = digits
                onChange((digits.toIntOrNull() ?: min).coerceIn(min, max))
            },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
            modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
        )
        OutlinedButton(
            onClick = { onChange((value + 1).coerceAtMost(max)) },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(44.dp),
        ) { Text("+", fontSize = 20.sp) }
    }
}

@Composable
fun EditorScreen(initial: Workout, onSave: (Workout) -> Unit, onCancel: () -> Unit) {
    var w by remember { mutableStateOf(initial) }
    val valid = w.name.isNotBlank() && w.segments.isNotEmpty() && w.intervalSeconds > 0 &&
        w.intervals >= 1 && w.sets >= 1

    Scaffold(
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                HorizontalDivider()
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Ein Intervall: ${formatTime(w.intervalSeconds)}   ·   Gesamt: ${formatTime(w.totalSeconds)}",
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Abbrechen") }
                        Button(onClick = { onSave(w) }, enabled = valid, modifier = Modifier.weight(1f)) { Text("Speichern") }
                    }
                }
            }
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Training bearbeiten", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = w.name, onValueChange = { w = w.copy(name = it) },
                label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            )

            Text("Ablauf", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            NumberField("Sätze", w.sets, { w = w.copy(sets = it) }, min = 1, max = 99)
            NumberField("Intervalle pro Satz", w.intervals, { w = w.copy(intervals = it) }, min = 1, max = 999)
            NumberField("Pause zwischen Sätzen (s)", w.setRestSeconds, { w = w.copy(setRestSeconds = it) })
            NumberField("Vorbereitung (s)", w.prepSeconds, { w = w.copy(prepSeconds = it) })
            NumberField("Cooldown am Ende (s)", w.cooldownSeconds, { w = w.copy(cooldownSeconds = it) })
            NumberField("Countdown-Piepser (letzte s)", w.countdownSeconds, { w = w.copy(countdownSeconds = it) }, max = 30)

            Text("Segmente pro Intervall", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            w.segments.forEachIndexed { i, seg ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = seg.name,
                            onValueChange = { v -> w = w.copy(segments = w.segments.replaceAt(i, seg.copy(name = v))) },
                            label = { Text("Bezeichnung") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        )
                        NumberField(
                            "Dauer (s)", seg.seconds,
                            { v -> w = w.copy(segments = w.segments.replaceAt(i, seg.copy(seconds = v))) },
                            min = 1,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = seg.type == SegmentType.WORK,
                                onClick = { w = w.copy(segments = w.segments.replaceAt(i, seg.copy(type = SegmentType.WORK))) },
                                label = { Text("Übung (rot)") },
                            )
                            FilterChip(
                                selected = seg.type == SegmentType.REST,
                                onClick = { w = w.copy(segments = w.segments.replaceAt(i, seg.copy(type = SegmentType.REST))) },
                                label = { Text("Pause (grün)") },
                            )
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { w = w.copy(segments = w.segments.moveBy(i, -1)) }, enabled = i > 0) { Text("▲") }
                            TextButton(onClick = { w = w.copy(segments = w.segments.moveBy(i, +1)) }, enabled = i < w.segments.lastIndex) { Text("▼") }
                            TextButton(
                                onClick = { w = w.copy(segments = w.segments.filterIndexed { j, _ -> j != i }) },
                                enabled = w.segments.size > 1,
                            ) { Text("✕") }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    w = w.copy(segments = w.segments + Segment("Übung", 30, SegmentType.WORK))
                }) { Text("+ Übung") }
                OutlinedButton(onClick = {
                    w = w.copy(segments = w.segments + Segment("Pause", 10, SegmentType.REST))
                }) { Text("+ Pause") }
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

// ---------------------------------------------------------------------------------------------
// Timer
// ---------------------------------------------------------------------------------------------

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
    val bg = if (state.finished) Color(0xFF263238) else phase.kind.color()
    val w = state.workout
    val secs = ((state.remainingMs + 999) / 1000).toInt()

    Box(Modifier.fillMaxSize().background(bg)) {
        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.finished) {
                Spacer(Modifier.weight(1f))
                Text("GESCHAFFT!", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                Text(
                    "${w.name}\n${w.sets} ${if (w.sets == 1) "Satz" else "Sätze"} · ${w.intervals} Intervalle · ${formatTime(w.totalSeconds)}",
                    color = Color.White, fontSize = 18.sp, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { TimerHolder.set(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) { Text("Fertig", fontSize = 18.sp) }
            } else {
                // Kopf: Satz / Intervall
                Text(
                    when {
                        phase.interval > 0 ->
                            (if (w.sets > 1) "Satz ${phase.set}/${w.sets}  ·  " else "") +
                                "Intervall ${phase.interval}/${w.intervals}"
                        else -> w.name
                    },
                    color = Color.White.copy(alpha = 0.9f), fontSize = 20.sp, fontWeight = FontWeight.Medium,
                )
                Text(
                    "Gesamt noch ${formatTime(((state.totalRemainingMs + 999) / 1000).toInt())}",
                    color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp,
                )

                Spacer(Modifier.weight(1f))
                Text(
                    phase.label.uppercase(),
                    color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                )
                Text(
                    if (secs >= 60) formatTime(secs) else secs.toString(),
                    color = Color.White, fontSize = 150.sp, fontWeight = FontWeight.Bold,
                )
                if (state.paused) {
                    Text("PAUSIERT", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))

                state.next?.let {
                    Text(
                        "Als Nächstes: ${it.label} (${it.seconds}s)",
                        color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp,
                    )
                } ?: Text("Letzter Abschnitt", color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp)
                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { TimerService.command(context, TimerService.ACTION_PREV) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) { Text("⏮", fontSize = 22.sp) }
                    Button(
                        onClick = { TimerService.command(context, TimerService.ACTION_TOGGLE_PAUSE) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        modifier = Modifier.weight(2f).height(56.dp),
                    ) { Text(if (state.paused) "Weiter" else "Pause", fontSize = 18.sp) }
                    OutlinedButton(
                        onClick = { TimerService.command(context, TimerService.ACTION_NEXT) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) { Text("⏭", fontSize = 22.sp) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        onClick = { sound = !sound; storage.soundOn = sound },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    ) { Text(if (sound) "Ton: an" else "Ton: aus") }
                    TextButton(
                        onClick = { vibration = !vibration; storage.vibrationOn = vibration },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    ) { Text(if (vibration) "Vibration: an" else "Vibration: aus") }
                    TextButton(
                        onClick = { confirmStop = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    ) { Text("Beenden") }
                }
            }
        }

        // Weißer Blitz bei jedem Segmentwechsel
        Box(Modifier.fillMaxSize().alpha(flash.value).background(Color.White))
    }

    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
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
