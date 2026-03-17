package com.github.senz.binaural

import android.content.Context
import android.media.AudioManager

/**
 * Helper for playback volume check. Used to show high-volume warning before starting playback.
 */
object VolumeCheck {
    const val WARNING_THRESHOLD_PERCENT = 30

    fun getMusicVolumePercent(context: Context): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max <= 0) return 0
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return current * 100 / max
    }
}
