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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinauralTheme {
                BinauralScreen(
                    isPlaying = isPlaying,
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
                                CountdownHelper.getInstance().startCountdown(wave!!, endTime, this)
                            }
                        }
                    },
                    onStopRequested = {
                        CountdownHelper.getInstance().cancelCountdown(this)
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
                                CountdownHelper.getInstance().startCountdown(wave!!, endTime, this)
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
            wave?.stop()
            isPlaying = false
        } else if (isPlaying && wave != null && !wave!!.getIsPlaying()) {
            CountdownHelper.getInstance().cancelCountdown(this)
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
