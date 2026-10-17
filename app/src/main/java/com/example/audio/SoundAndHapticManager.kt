package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Handles synthesized audio effects and haptic vibrations.
 * Does not depend on external audio files; generates crisp tones and sounds directly.
 */
class SoundAndHapticManager(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    /** Mala bead count haptic: distinct rhythmic tactile vibration on each bead click */
    fun playBeadVibration() {
        if (hapticsEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            } catch (_: Exception) {}
        }
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
            } catch (_: Exception) {}
        }
    }

    /** Subtle click haptic + light beep */
    fun playClick() {
        if (hapticsEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(20)
                }
            } catch (_: Exception) {}
        }
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
            } catch (_: Exception) {}
        }
    }

    /** Button click alias */
    fun playButtonClickSound() {
        playClick()
    }

    /** Coin earn / Reward chime */
    fun playCoinWin() {
        if (hapticsEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 60), intArrayOf(0, 180, 0, 255), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 30, 40, 60), -1)
                }
            } catch (_: Exception) {}
        }
        if (soundEnabled) {
            playSynthesizedChime(listOf(987.77, 1318.51, 1567.98), 65) // B5, E6, G6 cheerful coin chime
        }
    }

    /** Game loss vibration / buzz */
    fun playGameLoss() {
        if (hapticsEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(120)
                }
            } catch (_: Exception) {}
        }
        if (soundEnabled) {
            playSynthesizedChime(listOf(349.23, 311.13, 261.63), 90) // Descending tones
        }
    }

    /** Success chime on chapter complete, streak claim, or task complete */
    fun playSuccess() {
        if (hapticsEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 40, 55), intArrayOf(0, 180, 0, 255), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 35, 40, 55), -1)
                }
            } catch (_: Exception) {}
        }
        if (soundEnabled) {
            playSynthesizedChime(listOf(523.25, 659.25, 783.99, 1046.50), 75) // C5, E5, G5, C6 arpeggio
        }
    }

    /** Level up / Apex Tier celebration sound & heavy vibration */
    fun playLevelUp() {
        if (hapticsEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 70, 50, 120), intArrayOf(0, 180, 0, 220, 0, 255), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 50, 50, 70, 50, 120), -1)
                }
            } catch (_: Exception) {}
        }
        if (soundEnabled) {
            playSynthesizedChime(listOf(440.0, 554.37, 659.25, 880.0, 1108.73), 85)
        }
    }

    private fun playSynthesizedChime(frequencies: List<Double>, noteDurationMs: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                val totalSamples = ((noteDurationMs / 1000.0) * sampleRate * frequencies.size).toInt()
                val buffer = ShortArray(totalSamples)
                val samplesPerNote = (noteDurationMs / 1000.0 * sampleRate).toInt()
                var bufferIndex = 0

                frequencies.forEach { freq ->
                    for (i in 0 until samplesPerNote) {
                        if (bufferIndex >= buffer.size) break
                        val envelope = when {
                            i < 100 -> i / 100.0
                            i > samplesPerNote - 300 -> (samplesPerNote - i) / 300.0
                            else -> 1.0
                        }
                        val sample = (sin(2.0 * PI * i * freq / sampleRate) * envelope * Short.MAX_VALUE * 0.45).toInt()
                        buffer[bufferIndex++] = sample.toShort()
                    }
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                kotlinx.coroutines.delay((noteDurationMs * frequencies.size + 100).toLong())
                track.stop()
                track.release()
            } catch (_: Exception) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
            }
        }
    }
}
