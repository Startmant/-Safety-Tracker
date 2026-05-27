package com.example.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioSirenManager {
    private var audioTrack: AudioTrack? = null
    private var sirenJob: Job? = null
    private val sampleRate = 44100
    private var isPlaying = false

    fun startSiren(scope: CoroutineScope) {
        if (isPlaying) return
        isPlaying = true

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize.coerceAtLeast(sampleRate / 4),
            AudioTrack.MODE_STREAM
        )

        audioTrack?.play()

        sirenJob = scope.launch(Dispatchers.Default) {
            val numSamples = 4410 // 100ms chunks
            val samples = ShortArray(numSamples)
            var angle = 0.0
            var timePassed = 0.0

            while (isPlaying) {
                // Synthesize dual tone alternating siren (Wailing Siren effect)
                // Pitch moves sinusoidally between 600Hz and 1200Hz every 1.5 seconds
                val sweepFreq = 600.0 + 300.0 * (1.0 + sin(2 * Math.PI * timePassed / 1.5))
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    samples[i] = (sin(angle + 2 * Math.PI * sweepFreq * t) * Short.MAX_VALUE * 0.9).toInt().toShort()
                }
                
                angle += 2 * Math.PI * sweepFreq * (numSamples.toDouble() / sampleRate)
                angle %= (2 * Math.PI)
                
                audioTrack?.write(samples, 0, numSamples)
                timePassed += (numSamples.toDouble() / sampleRate)
                delay(95) // Feed chunk sequentially
            }
        }
    }

    fun stopSiren() {
        isPlaying = false
        sirenJob?.cancel()
        sirenJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
    }
}
