package com.guidedmeditationtreks.binaural;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives the countdown-end alarm and stops playback via CountdownHelper.
 */
public class CountdownAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (!CountdownHelper.getCountdownEndAction().equals(intent.getAction())) return;
        CountdownHelper.getInstance().stopFromAlarm();
    }
}
