package de.intervaltimer.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            AppTheme { App() }
        }
    }
}

private sealed interface Screen {
    data object Home : Screen
    data class Edit(val workout: Workout, val isNew: Boolean) : Screen
}

@Composable
private fun App() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val storage = remember { Storage(context) }
    var workouts by remember { mutableStateOf(storage.loadWorkouts()) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    val run by TimerHolder.state.collectAsStateWithLifecycle()

    var pendingStart by remember { mutableStateOf<Workout?>(null) }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Der Timer läuft auch ohne Benachrichtigungs-Erlaubnis; sie zeigt nur die Restzeit an.
        pendingStart?.let { TimerService.start(context, it) }
        pendingStart = null
    }

    fun startWorkout(w: Workout) {
        val needs = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needs) {
            pendingStart = w
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            TimerService.start(context, w)
        }
    }

    fun persist(list: List<Workout>) {
        workouts = list
        storage.saveWorkouts(list)
    }

    val current = run
    if (current != null) {
        TimerScreen(current, storage)
        return
    }

    when (val s = screen) {
        Screen.Home -> HomeScreen(
            workouts = workouts,
            onStart = ::startWorkout,
            onEdit = { screen = Screen.Edit(it, isNew = false) },
            onNew = {
                val id = (workouts.maxOfOrNull { it.id } ?: 0L) + 1
                screen = Screen.Edit(Workout(id = id, name = "Neues Training"), isNew = true)
            },
            onDuplicate = { w ->
                val id = (workouts.maxOfOrNull { it.id } ?: 0L) + 1
                persist(workouts + w.copy(id = id, name = w.name + " (Kopie)"))
            },
            onDelete = { w -> persist(workouts.filterNot { it.id == w.id }) },
        )
        is Screen.Edit -> {
            BackHandler { screen = Screen.Home }
            EditorScreen(
                initial = s.workout,
                onSave = { saved ->
                    persist(if (s.isNew) workouts + saved else workouts.map { if (it.id == saved.id) saved else it })
                    screen = Screen.Home
                },
                onCancel = { screen = Screen.Home },
            )
        }
    }
}

/** Hält den Bildschirm an, solange der Composable aktiv ist. */
@Composable
fun KeepScreenOn() {
    val context = androidx.compose.ui.platform.LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? ComponentActivity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}
