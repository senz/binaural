package com.guidedmeditationtreks.binaural;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Holds the current playback and countdown end time so that AlarmManager's
 * BroadcastReceiver can stop playback when the timer fires (even when app is in background).
 */
public final class CountdownHelper {

    private static final String ACTION_COUNTDOWN_END = "com.guidedmeditationtreks.binaural.COUNTDOWN_END";
    private static final int REQUEST_CODE_ALARM = 1;

    private static volatile CountdownHelper instance;
    private volatile BeatsEngine wave;
    private volatile long endTimeMillis;
    private volatile boolean countdownActive;
    private volatile PendingIntent alarmPendingIntent;
    private volatile Context appContext;

    public static CountdownHelper getInstance() {
        if (instance == null) {
            synchronized (CountdownHelper.class) {
                if (instance == null) {
                    instance = new CountdownHelper();
                }
            }
        }
        return instance;
    }

    public void setContext(Context context) {
        this.appContext = context != null ? context.getApplicationContext() : null;
    }

    /**
     * Register current wave and schedule alarm to stop at endTimeMillis.
     * Call from MainActivity when starting countdown.
     */
    public void startCountdown(BeatsEngine wave, long endTimeMillis, Context context) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        this.appContext = app;
        this.wave = wave;
        this.endTimeMillis = endTimeMillis;
        this.countdownActive = true;

        cancelAlarm(app);

        Intent intent = new Intent(ACTION_COUNTDOWN_END);
        intent.setPackage(app.getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        alarmPendingIntent = PendingIntent.getBroadcast(app, REQUEST_CODE_ALARM, intent, flags);

        AlarmManager alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeMillis, alarmPendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeMillis, alarmPendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, endTimeMillis, alarmPendingIntent);
            }
        }
    }

    /**
     * Cancel the countdown alarm and clear reference. Call when user stops manually or Activity is destroyed.
     */
    public void cancelCountdown(Context context) {
        if (context != null) {
            cancelAlarm(context.getApplicationContext());
        } else if (appContext != null) {
            cancelAlarm(appContext);
        }
        wave = null;
        endTimeMillis = 0;
        countdownActive = false;
        alarmPendingIntent = null;
    }

    private void cancelAlarm(Context app) {
        if (alarmPendingIntent == null || app == null) return;
        AlarmManager alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(alarmPendingIntent);
        }
        alarmPendingIntent = null;
    }

    /**
     * Called from CountdownAlarmReceiver when the alarm fires. Stops playback and clears state.
     */
    public void stopFromAlarm() {
        BeatsEngine w = wave;
        wave = null;
        endTimeMillis = 0;
        countdownActive = false;
        alarmPendingIntent = null;
        if (appContext != null) {
            cancelAlarm(appContext);
        }
        if (w != null) {
            try {
                w.stop();
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isCountdownActive() {
        return countdownActive;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    /** Whether countdown has already ended (for UI sync in onResume). */
    public boolean hasCountdownEnded() {
        return countdownActive && endTimeMillis > 0 && System.currentTimeMillis() >= endTimeMillis;
    }

    public static String getCountdownEndAction() {
        return ACTION_COUNTDOWN_END;
    }
}
