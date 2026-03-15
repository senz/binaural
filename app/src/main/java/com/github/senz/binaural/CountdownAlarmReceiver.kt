package com.github.senz.binaural

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives the countdown-end alarm and stops playback via CountdownHelper.
 */
class CountdownAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        if (context == null || intent == null) return
        if (CountdownHelper.getCountdownEndAction() != intent.action) return
        CountdownHelper.stopFromAlarm()
    }
}
