package de.intervaltimer.app

import android.content.Context
import org.json.JSONArray

class Storage(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("interval_timer", Context.MODE_PRIVATE)

    fun loadWorkouts(): List<Workout> {
        val raw = prefs.getString("workouts", null) ?: return seed().also { saveWorkouts(it) }
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { Workout.fromJson(arr.getJSONObject(it)) }
        }.getOrElse { seed() }
    }

    fun saveWorkouts(list: List<Workout>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("workouts", arr.toString()).apply()
    }

    var soundOn: Boolean
        get() = prefs.getBoolean("sound", true)
        set(v) = prefs.edit().putBoolean("sound", v).apply()

    var vibrationOn: Boolean
        get() = prefs.getBoolean("vibration", true)
        set(v) = prefs.edit().putBoolean("vibration", v).apply()

    private fun seed(): List<Workout> = listOf(
        Workout(
            id = 1, name = "Klassisch 45/15", prepSeconds = 10, intervals = 10,
            segments = listOf(
                Segment("Übung", 45, SegmentType.WORK),
                Segment("Pause", 15, SegmentType.REST),
            ),
        ),
        Workout(
            id = 2, name = "Tabata 20/10", prepSeconds = 10, intervals = 8, sets = 3, setRestSeconds = 60,
            segments = listOf(
                Segment("Übung", 20, SegmentType.WORK),
                Segment("Pause", 10, SegmentType.REST),
            ),
        ),
    )
}
