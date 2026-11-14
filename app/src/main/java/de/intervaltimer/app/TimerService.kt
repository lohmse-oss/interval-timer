package de.intervaltimer.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/** Zustand eines laufenden Trainings, vom UI beobachtet. */
data class RunState(
    val workout: Workout,
    val phases: List<Phase>,
    val index: Int,
    val remainingMs: Long,
    val paused: Boolean,
    val finished: Boolean,
    /** Wird bei jedem Phasenwechsel hochgezählt (löst den Flash aus). */
    val changeCount: Int,
    val totalRemainingMs: Long,
) {
    val phase: Phase get() = phases[index.coerceIn(0, phases.lastIndex)]
    val next: Phase? get() = phases.getOrNull(index + 1)
}

object TimerHolder {
    private val _state = MutableStateFlow<RunState?>(null)
    val state: StateFlow<RunState?> = _state
    internal fun set(s: RunState?) { _state.value = s }
}

class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loop: Job? = null
    private lateinit var feedback: Feedback
    private lateinit var storage: Storage
    private var wakeLock: PowerManager.WakeLock? = null

    private var workout: Workout? = null
    private var phases: List<Phase> = emptyList()
    private var suffixMs: LongArray = LongArray(0) // Dauer aller Phasen NACH Index i
    private var index = 0
    private var phaseEndsAt = 0L
    private var pausedRemaining = 0L
    private var paused = false
    private var changeCount = 0
    private var lastTickSec = -1
    private var lastNotifText = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        feedback = Feedback(this)
        storage = Storage(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val json = intent.getStringExtra(EXTRA_WORKOUT) ?: return START_NOT_STICKY
                start(Workout.fromJson(JSONObject(json)))
            }
            ACTION_TOGGLE_PAUSE -> togglePause()
            ACTION_NEXT -> jumpTo(index + 1)
            ACTION_PREV -> restartOrPrevious()
            ACTION_STOP -> stopAll()
        }
        return START_NOT_STICKY
    }

    // ---- Ablauf -------------------------------------------------------------------------------

    private fun start(w: Workout) {
        loop?.cancel()
        workout = w
        phases = buildPhases(w)
        if (phases.isEmpty()) { stopAll(); return }
        suffixMs = LongArray(phases.size)
        var acc = 0L
        for (i in phases.indices.reversed()) {
            suffixMs[i] = acc
            acc += phases[i].seconds * 1000L
        }
        index = 0
        paused = false
        changeCount = 0
        enterPhase(0, announce = false)
        acquireWakeLock()
        startForegroundCompat(buildNotification())
        publish(finished = false)
        loop = scope.launch {
            while (true) {
                tick()
                delay(50)
            }
        }
    }

    private fun enterPhase(i: Int, announce: Boolean, carryMs: Long = 0) {
        index = i
        lastTickSec = -1
        phaseEndsAt = SystemClock.elapsedRealtime() + phases[i].seconds * 1000L + carryMs
        if (announce) {
            changeCount++
            feedback.change(storage.soundOn, storage.vibrationOn)
        }
    }

    private fun tick() {
        if (paused) return
        val now = SystemClock.elapsedRealtime()
        var remaining = phaseEndsAt - now
        if (remaining <= 0) {
            val nextIdx = index + 1
            if (nextIdx >= phases.size) {
                finishWorkout()
                return
            }
            // Überhang übernehmen, damit sich kleine Verzögerungen nicht aufsummieren
            enterPhase(nextIdx, announce = true, carryMs = remaining.coerceAtLeast(-200))
            remaining = phaseEndsAt - now
        }
        val sec = ((remaining + 999) / 1000).toInt()
        val w = workout!!
        val p = phases[index]
        if (sec != lastTickSec) {
            if (sec in 1..w.countdownSeconds && sec < p.seconds) {
                feedback.tick(storage.soundOn, storage.vibrationOn)
            }
            lastTickSec = sec
            updateNotification()
        }
        publish(finished = false, remaining = remaining)
    }

    private fun togglePause() {
        if (workout == null || TimerHolder.state.value?.finished == true) return
        val now = SystemClock.elapsedRealtime()
        if (paused) {
            phaseEndsAt = now + pausedRemaining
            paused = false
        } else {
            pausedRemaining = (phaseEndsAt - now).coerceAtLeast(0)
            paused = true
        }
        publish(finished = false, remaining = if (paused) pausedRemaining else phaseEndsAt - now)
        updateNotification(force = true)
    }

    private fun jumpTo(target: Int) {
        if (workout == null) return
        if (target >= phases.size) { finishWorkout(); return }
        val wasPaused = paused
        enterPhase(target.coerceAtLeast(0), announce = true)
        if (wasPaused) pausedRemaining = phases[index].seconds * 1000L
        publish(finished = false, remaining = if (paused) pausedRemaining else phaseEndsAt - SystemClock.elapsedRealtime())
        updateNotification(force = true)
    }

    /** Wenn mehr als 2 s in der Phase vergangen sind: Phase neu starten, sonst eine zurück. */
    private fun restartOrPrevious() {
        if (workout == null) return
        val elapsed = phases[index].seconds * 1000L - currentRemaining()
        jumpTo(if (elapsed > 2000) index else index - 1)
    }

    private fun currentRemaining(): Long =
        if (paused) pausedRemaining else (phaseEndsAt - SystemClock.elapsedRealtime()).coerceAtLeast(0)

    private fun finishWorkout() {
        loop?.cancel()
        feedback.finish(storage.soundOn, storage.vibrationOn)
        index = phases.lastIndex
        changeCount++
        publish(finished = true, remaining = 0)
        releaseWakeLock()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopAll() {
        loop?.cancel()
        TimerHolder.set(null)
        releaseWakeLock()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun publish(finished: Boolean, remaining: Long = currentRemaining()) {
        val w = workout ?: return
        TimerHolder.set(
            RunState(
                workout = w,
                phases = phases,
                index = index,
                remainingMs = remaining.coerceAtLeast(0),
                paused = paused,
                finished = finished,
                changeCount = changeCount,
                totalRemainingMs = if (finished) 0 else remaining.coerceAtLeast(0) + suffixMs[index],
            ),
        )
    }

    // ---- Benachrichtigung ----------------------------------------------------------------------

    private fun createChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Laufender Timer", NotificationManager.IMPORTANCE_LOW),
        )
    }

    private fun pending(action: String, code: Int): PendingIntent =
        PendingIntent.getService(
            this, code, Intent(this, TimerService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun buildNotification(): Notification {
        val p = phases.getOrNull(index)
        val w = workout
        val title = if (p == null || w == null) "Intervall-Timer" else
            "${p.label} · ${formatTime(((currentRemaining() + 999) / 1000).toInt())}"
        val sub = if (p == null || w == null) "" else when {
            p.interval > 0 -> "Satz ${p.set}/${w.sets} · Intervall ${p.interval}/${w.intervals}" + if (paused) " · pausiert" else ""
            else -> w.name + if (paused) " · pausiert" else ""
        }
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(title)
            .setContentText(sub)
            .setContentIntent(open)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(0, if (paused) "Weiter" else "Pause", pending(ACTION_TOGGLE_PAUSE, 1))
            .addAction(0, "Stopp", pending(ACTION_STOP, 2))
            .build()
    }

    private fun updateNotification(force: Boolean = false) {
        val n = buildNotification()
        val text = n.extras.getString(Notification.EXTRA_TITLE) + "|" + n.extras.getString(Notification.EXTRA_TEXT)
        if (!force && text == lastNotifText) return
        lastNotifText = text
        runCatching {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIF_ID, n)
        }
    }

    private fun startForegroundCompat(n: Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(this, NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    // ---- Wake Lock ------------------------------------------------------------------------------

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IntervalTimer:run").apply {
            setReferenceCounted(false)
            acquire(6 * 60 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
    }

    override fun onDestroy() {
        releaseWakeLock()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "timer"
        private const val NOTIF_ID = 42
        const val ACTION_START = "de.intervaltimer.START"
        const val ACTION_TOGGLE_PAUSE = "de.intervaltimer.TOGGLE_PAUSE"
        const val ACTION_NEXT = "de.intervaltimer.NEXT"
        const val ACTION_PREV = "de.intervaltimer.PREV"
        const val ACTION_STOP = "de.intervaltimer.STOP"
        private const val EXTRA_WORKOUT = "workout"

        fun start(context: Context, w: Workout) {
            val i = Intent(context, TimerService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_WORKOUT, w.toJson().toString())
            androidx.core.content.ContextCompat.startForegroundService(context, i)
        }

        fun command(context: Context, action: String) {
            // Direkt per startService: Der Service läuft bereits im Vordergrund.
            context.startService(Intent(context, TimerService::class.java).setAction(action))
        }
    }
}
