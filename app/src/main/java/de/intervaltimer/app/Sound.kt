package de.intervaltimer.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.sin

/** Erzeugt Pieptöne als Sinuswelle, ohne Audiodateien. */
class Feedback(private val context: Context) {
    private val rate = 22050

    private fun pcm(freq: Double, ms: Int): ShortArray {
        val n = rate * ms / 1000
        val fade = (rate * 0.008).toInt().coerceAtLeast(1)
        return ShortArray(n) { i ->
            var amp = 0.9
            if (i < fade) amp *= i / fade.toDouble()
            if (i > n - fade) amp *= (n - i) / fade.toDouble()
            (sin(2 * PI * freq * i / rate) * amp * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private val tick = pcm(880.0, 110)
    private val change = pcm(1320.0, 450)
    private val finish = pcm(1320.0, 180) + ShortArray(rate / 12) + pcm(1320.0, 180) +
        ShortArray(rate / 12) + pcm(1760.0, 700)

    private fun play(data: ShortArray) {
        runCatching {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(rate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(data.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(data, 0, data.size)
            track.setNotificationMarkerPosition(data.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack) { t.release() }
                override fun onPeriodicNotification(t: AudioTrack) {}
            })
            track.play()
        }
    }

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun vibrate(ms: Long) {
        runCatching { vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)) }
    }

    fun tick(sound: Boolean, vibration: Boolean) {
        if (sound) play(tick)
        if (vibration) vibrate(40)
    }

    fun change(sound: Boolean, vibration: Boolean) {
        if (sound) play(change)
        if (vibration) vibrate(300)
    }

    fun finish(sound: Boolean, vibration: Boolean) {
        if (sound) play(finish)
        if (vibration) {
            runCatching {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 120, 200, 120, 500), -1))
            }
        }
    }
}
