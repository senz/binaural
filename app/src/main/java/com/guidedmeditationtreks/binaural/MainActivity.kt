package com.guidedmeditationtreks.binaural

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private var wave: BeatsEngine? = null
    private var isPlaying by mutableStateOf(false)
        private set
    private var timerEndTimeMillis by mutableStateOf(0L)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinauralTheme {
                BinauralScreen(
                    isPlaying = isPlaying,
                    timerEndTimeMillis = timerEndTimeMillis,
                    onPlayRequested = { carrier: Float, beat: Float, isBinaural: Boolean, timerMinutes: Int? ->
                        CountdownHelper.getInstance().cancelCountdown(this)
                        wave?.release()
                        wave =
                            if (isBinaural) {
                                Binaural(carrier, beat)
                            } else {
                                Isochronic(carrier, beat)
                            }
                        if (!wave!!.getIsPlaying()) {
                            wave!!.start()
                            isPlaying = true
                            if (timerMinutes != null && timerMinutes > 0) {
                                val endTime = System.currentTimeMillis() + timerMinutes * 60_000L
                                timerEndTimeMillis = endTime
                                CountdownHelper.getInstance().startCountdown(wave!!, endTime, this)
                            } else {
                                timerEndTimeMillis = 0L
                            }
                        }
                    },
                    onStopRequested = {
                        CountdownHelper.getInstance().cancelCountdown(this)
                        timerEndTimeMillis = 0L
                        wave?.stop()
                        isPlaying = false
                    },
                    onRefreshAndRestart = { carrier: Float, beat: Float, isBinaural: Boolean, timerMinutes: Int? ->
                        if (!isPlaying) return@BinauralScreen
                        CountdownHelper.getInstance().cancelCountdown(this)
                        wave?.release()
                        wave =
                            if (isBinaural) {
                                Binaural(carrier, beat)
                            } else {
                                Isochronic(carrier, beat)
                            }
                        if (!wave!!.getIsPlaying()) {
                            wave!!.start()
                            if (timerMinutes != null && timerMinutes > 0) {
                                val endTime = System.currentTimeMillis() + timerMinutes * 60_000L
                                timerEndTimeMillis = endTime
                                CountdownHelper.getInstance().startCountdown(wave!!, endTime, this)
                            } else {
                                timerEndTimeMillis = 0L
                            }
                        }
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (CountdownHelper.getInstance().hasCountdownEnded()) {
            CountdownHelper.getInstance().stopFromAlarm()
            timerEndTimeMillis = 0L
            wave?.stop()
            isPlaying = false
        } else if (isPlaying && wave != null && !wave!!.getIsPlaying()) {
            CountdownHelper.getInstance().cancelCountdown(this)
            timerEndTimeMillis = 0L
            isPlaying = false
        }
    }

    override fun onDestroy() {
        CountdownHelper.getInstance().cancelCountdown(this)
        wave?.release()
        wave = null
        super.onDestroy()
    }
}
