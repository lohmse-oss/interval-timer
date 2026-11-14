package de.intervaltimer.app

import org.json.JSONArray
import org.json.JSONObject

enum class SegmentType { WORK, REST }

data class Segment(
    val name: String,
    val seconds: Int,
    val type: SegmentType,
) {
    /** Leerer Name → „Übung“ bzw. „Pause“. */
    val displayName: String
        get() = name.trim().ifBlank { if (type == SegmentType.WORK) "Übung" else "Pause" }
}

/**
 * Ein Training besteht aus [sets] Sätzen. Jeder Satz hat [intervals] Intervalle,
 * jedes Intervall besteht aus den [segments] (z. B. Übung 45 s + Pause 15 s).
 */
data class Workout(
    val id: Long,
    val name: String,
    val prepSeconds: Int = 10,
    val intervals: Int = 10,
    val segments: List<Segment> = listOf(
        Segment("Übung", 45, SegmentType.WORK),
        Segment("Pause", 15, SegmentType.REST),
    ),
    val sets: Int = 1,
    val setRestSeconds: Int = 60,
    val cooldownSeconds: Int = 0,
    val countdownSeconds: Int = 5,
) {
    val intervalSeconds: Int get() = segments.sumOf { it.seconds }

    val totalSeconds: Int
        get() = prepSeconds +
            sets * intervals * intervalSeconds +
            (sets - 1).coerceAtLeast(0) * setRestSeconds +
            cooldownSeconds

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("prep", prepSeconds)
        put("intervals", intervals)
        put("sets", sets)
        put("setRest", setRestSeconds)
        put("cooldown", cooldownSeconds)
        put("countdown", countdownSeconds)
        put("segments", JSONArray().also { arr ->
            segments.forEach {
                arr.put(JSONObject().put("name", it.name).put("sec", it.seconds).put("type", it.type.name))
            }
        })
    }

    companion object {
        fun fromJson(o: JSONObject): Workout {
            val segs = o.getJSONArray("segments")
            return Workout(
                id = o.getLong("id"),
                name = o.getString("name"),
                prepSeconds = o.optInt("prep", 10),
                intervals = o.optInt("intervals", 1),
                sets = o.optInt("sets", 1),
                setRestSeconds = o.optInt("setRest", 60),
                cooldownSeconds = o.optInt("cooldown", 0),
                countdownSeconds = o.optInt("countdown", 5),
                segments = (0 until segs.length()).map {
                    val s = segs.getJSONObject(it)
                    Segment(
                        s.getString("name"),
                        s.getInt("sec"),
                        runCatching { SegmentType.valueOf(s.getString("type")) }.getOrDefault(SegmentType.WORK),
                    )
                },
            )
        }
    }
}

enum class PhaseKind { PREP, WORK, REST, SET_REST, COOLDOWN }

/** Ein einzelner Abschnitt im abgeflachten Ablaufplan. */
data class Phase(
    val kind: PhaseKind,
    val label: String,
    val seconds: Int,
    val set: Int,        // 1-basiert, 0 bei Vorbereitung/Cooldown
    val interval: Int,   // 1-basiert, 0 bei Vorbereitung/Satzpause/Cooldown
)

/** Wandelt ein Training in die lineare Liste aller Phasen um. */
fun buildPhases(w: Workout): List<Phase> {
    val out = mutableListOf<Phase>()
    if (w.prepSeconds > 0) out += Phase(PhaseKind.PREP, "Bereit machen", w.prepSeconds, 0, 0)
    for (s in 1..w.sets) {
        for (i in 1..w.intervals) {
            for (seg in w.segments) {
                if (seg.seconds <= 0) continue
                out += Phase(
                    if (seg.type == SegmentType.WORK) PhaseKind.WORK else PhaseKind.REST,
                    seg.displayName, seg.seconds, s, i,
                )
            }
        }
        if (s < w.sets && w.setRestSeconds > 0) {
            out += Phase(PhaseKind.SET_REST, "Satzpause", w.setRestSeconds, s, 0)
        }
    }
    if (w.cooldownSeconds > 0) out += Phase(PhaseKind.COOLDOWN, "Cooldown", w.cooldownSeconds, 0, 0)
    return out
}

fun formatTime(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
