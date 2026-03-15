package com.github.senz.binaural

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper

class Binaural(
    frequency: Float,
    isoBeat: Float,
    private val listener: BeatsEngineListener?,
) : BeatsEngine {
    private val sampleRate = 44100
    private val sampleCount: Int
    private val mAudio: AudioTrack

    private var doRelease = false
    private var _isPlaying = false

    private val handler = Handler(Looper.getMainLooper())
    private var pendingVolumeUp: Runnable? = null
    private var pendingStop: Runnable? = null

    override val isPlaying: Boolean get() = _isPlaying

    init {
        val samples = Companion.generateSamples(frequency, isoBeat, sampleRate)
        sampleCount = samples.size
        val buffSize = sampleCount * 2

        mAudio =
            AudioTrack
                .Builder()
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                ).setAudioFormat(
                    AudioFormat
                        .Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build(),
                ).setBufferSizeInBytes(buffSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

        mAudio.write(samples, 0, sampleCount)
        mAudio.setVolume(0f)
    }

    override fun release() {
        doRelease = true
        cancelPending()
        stop()
    }

    override fun start() {
        cancelPending()
        mAudio.reloadStaticData()
        mAudio.setLoopPoints(0, sampleCount / 2, -1)
        _isPlaying = true
        mAudio.play()

        val runnable =
            Runnable {
                pendingVolumeUp = null
                if (doRelease) return@Runnable
                mAudio.setVolume(1f)
                listener?.onStarted(this@Binaural)
            }
        pendingVolumeUp = runnable
        handler.postDelayed(runnable, VOLUME_RAMP_MS)
    }

    override fun stop() {
        cancelPending()
        mAudio.setVolume(0f)

        val runnable =
            Runnable {
                pendingStop = null
                mAudio.stop()
                _isPlaying = false
                if (doRelease) {
                    mAudio.flush()
                    mAudio.release()
                }
                listener?.onStopped(this)
            }
        pendingStop = runnable
        handler.postDelayed(runnable, VOLUME_RAMP_MS)
    }

    private fun cancelPending() {
        pendingVolumeUp?.let { handler.removeCallbacks(it) }
        pendingVolumeUp = null
        pendingStop?.let { handler.removeCallbacks(it) }
        pendingStop = null
    }

    companion object {
        private const val VOLUME_RAMP_MS = 50L

        /**
         * Pure sample generation (no AudioTrack). Used by the class and by unit tests.
         */
        internal fun generateSamples(
            frequency: Float,
            isoBeat: Float,
            sampleRate: Int = 44100,
        ): ShortArray {
            val amplitudeMax = Helpers.getAdjustedAmplitudeMax(frequency)
            val freqLeft = frequency - (isoBeat / 2)
            val freqRight = frequency + (isoBeat / 2)
            val sCountLeft = (sampleRate / freqLeft).toInt()
            val sCountRight = (sampleRate / freqRight).toInt()
            val sampleCount = Helpers.getLCM(sCountLeft, sCountRight) * 2
            val twopi = 8.0 * Math.atan(1.0)
            var leftPhase = 0.0
            var rightPhase = 0.0
            val amplitude = amplitudeMax
            val samples = ShortArray(sampleCount)
            for (i in 0 until sampleCount step 2) {
                samples[i] = (amplitude * Math.sin(leftPhase)).toInt().toShort()
                samples[i + 1] = (amplitude * Math.sin(rightPhase)).toInt().toShort()
                if (i / 2 % sCountLeft == 0) leftPhase = 0.0
                leftPhase += twopi * freqLeft / sampleRate
                if (i / 2 % sCountRight == 0) rightPhase = 0.0
                rightPhase += twopi * freqRight / sampleRate
            }
            return samples
        }
    }
}
