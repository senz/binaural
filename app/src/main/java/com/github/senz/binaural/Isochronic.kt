package com.github.senz.binaural

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

class Isochronic(
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
    private var underrunPollRunnable: Runnable? = null
    private var lastUnderrunCount: Int = 0

    override val isPlaying: Boolean get() = _isPlaying

    init {
        val periodSamples = Companion.generateSamples(frequency, isoBeat, sampleRate)
        sampleCount = periodSamples.size
        val minBufferSize =
            AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val buffSize = maxOf(sampleCount * 2, minBufferSize)
        val writeSamples = buffSize / 2

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
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                ).setBufferSizeInBytes(buffSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

        if (writeSamples <= sampleCount) {
            mAudio.write(periodSamples, 0, writeSamples)
        } else {
            val buffer = ShortArray(writeSamples)
            var offset = 0
            while (offset < writeSamples) {
                val toCopy = minOf(sampleCount, writeSamples - offset)
                periodSamples.copyInto(buffer, offset, 0, toCopy)
                offset += toCopy
            }
            mAudio.write(buffer, 0, writeSamples)
        }
        mAudio.setVolume(0f)
    }

    override fun release() {
        doRelease = true
        stopUnderrunPolling()
        cancelPending()
        stop()
    }

    override fun start() {
        cancelPending()
        mAudio.reloadStaticData()
        mAudio.setLoopPoints(0, sampleCount, -1)
        _isPlaying = true
        mAudio.play()

        val runnable =
            Runnable {
                pendingVolumeUp = null
                if (doRelease) return@Runnable
                mAudio.setVolume(1f)
                listener?.onStarted(this@Isochronic)
            }
        pendingVolumeUp = runnable
        handler.postDelayed(runnable, VOLUME_RAMP_MS)
        startUnderrunPolling()
    }

    override fun stop() {
        stopUnderrunPolling()
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
                listener?.onStopped(this@Isochronic)
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

    /** Polls getUnderrunCount() and logs to Logcat when it increases. Active only in debug builds. Overrun is not exposed by AudioTrack for MODE_STATIC. */
    private fun startUnderrunPolling() {
        stopUnderrunPolling()
        if (!BuildConfig.DEBUG || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        lastUnderrunCount = mAudio.getUnderrunCount()
        Log.d(TAG, "buffer underrun polling started (overrun not in API for static mode)")
        underrunPollRunnable =
            object : Runnable {
                override fun run() {
                    if (doRelease || !_isPlaying) return
                    val count = mAudio.getUnderrunCount()
                    if (count > lastUnderrunCount) {
                        Log.w(TAG, "buffer underrun: count=$count (was $lastUnderrunCount)")
                        lastUnderrunCount = count
                    }
                    underrunPollRunnable?.let { handler.postDelayed(it, UNDERRUN_POLL_MS) }
                }
            }
        handler.postDelayed(underrunPollRunnable!!, UNDERRUN_POLL_MS)
    }

    private fun stopUnderrunPolling() {
        underrunPollRunnable?.let { handler.removeCallbacks(it) }
        underrunPollRunnable = null
    }

    companion object {
        private const val TAG = "Isochronic"
        private const val VOLUME_RAMP_MS = 50L
        private const val UNDERRUN_POLL_MS = 1500L
        private const val FADER = 128
        private const val AMPLITUDE_INCREMENT = 256

        /**
         * Pure sample generation (no AudioTrack). Used by the class and by unit tests.
         */
        internal fun generateSamples(
            frequency: Float,
            isoBeat: Float,
            sampleRate: Int = 44100,
        ): ShortArray {
            val amplitudeMax = Helpers.getAdjustedAmplitudeMax(frequency)
            val sCount = (sampleRate / frequency).toInt()
            val multiplier = (sampleRate / 2 / isoBeat).toInt()
            val sampleCount = multiplier * 2
            val twopi = 8.0 * Math.atan(1.0)
            var phase = 0.0
            var amplitude = 0
            var isOn = true
            var frame = 0
            val samples = ShortArray(sampleCount)
            for (i in 0 until sampleCount) {
                frame++
                if (frame == multiplier) {
                    amplitude = if (isOn) amplitudeMax else 0
                    frame = 0
                    isOn = !isOn
                } else if (frame <= FADER) {
                    amplitude =
                        when {
                            isOn -> minOf(amplitudeMax, amplitude + AMPLITUDE_INCREMENT)
                            else -> maxOf(0, amplitude - AMPLITUDE_INCREMENT)
                        }
                }
                samples[i] = (amplitude * Math.sin(phase)).toInt().toShort()
                if (i % sCount == 0) phase = 0.0
                phase += twopi * frequency / sampleRate
            }
            return samples
        }
    }
}
