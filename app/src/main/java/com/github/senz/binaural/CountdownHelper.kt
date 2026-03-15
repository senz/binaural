package com.github.senz.binaural

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Holds the current playback and countdown end time so that AlarmManager's
 * BroadcastReceiver can stop playback when the timer fires (even when app is in background).
 */
object CountdownHelper {
    private const val ACTION_COUNTDOWN_END = "com.github.senz.binaural.COUNTDOWN_END"
    private const val REQUEST_CODE_ALARM = 1

    @Volatile
    private var wave: BeatsEngine? = null

    @Volatile
    private var endTimeMillis: Long = 0L

    @Volatile
    private var countdownActive = false

    @Volatile
    private var alarmPendingIntent: PendingIntent? = null

    @Volatile
    private var appContext: Context? = null

    fun setContext(context: Context?) {
        appContext = context?.applicationContext
    }

    /**
     * Register current wave and schedule alarm to stop at endTimeMillis.
     * Call from PlaybackService when starting countdown.
     */
    fun startCountdown(
        wave: BeatsEngine,
        endTimeMillis: Long,
        context: Context,
    ) {
        val app = context.applicationContext
        appContext = app
        this.wave = wave
        this.endTimeMillis = endTimeMillis
        countdownActive = true

        cancelAlarm(app)

        val intent = Intent(ACTION_COUNTDOWN_END).setPackage(app.packageName)
        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pending = PendingIntent.getBroadcast(app, REQUEST_CODE_ALARM, intent, flags)
        alarmPendingIntent = pending

        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager != null) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms() ->
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeMillis, pending)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeMillis, pending)
                else ->
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, endTimeMillis, pending)
            }
        }
    }

    /**
     * Cancel the countdown alarm and clear reference. Call when user stops manually or service stops.
     */
    fun cancelCountdown(context: Context?) {
        when {
            context != null -> cancelAlarm(context.applicationContext)
            else -> appContext?.let { cancelAlarm(it) }
        }
        wave = null
        endTimeMillis = 0L
        countdownActive = false
        alarmPendingIntent = null
    }

    private fun cancelAlarm(app: Context?) {
        val pending = alarmPendingIntent
        if (pending == null || app == null) return
        (app.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.cancel(pending)
        alarmPendingIntent = null
    }

    /**
     * Called from CountdownAlarmReceiver when the alarm fires. Stops playback and clears state.
     * Notifies PlaybackService to stop foreground and finish.
     */
    fun stopFromAlarm() {
        val w = wave
        wave = null
        endTimeMillis = 0L
        countdownActive = false
        alarmPendingIntent = null
        appContext?.let { cancelAlarm(it) }
        try {
            w?.stop()
        } catch (_: Exception) {
        }
        appContext?.let { ctx ->
            val stopIntent = Intent(ctx, PlaybackService::class.java).setAction(PlaybackService.ACTION_STOP)
            ctx.startService(stopIntent)
        }
    }

    fun isCountdownActive(): Boolean = countdownActive

    fun getEndTimeMillis(): Long = endTimeMillis

    /** Whether countdown has already ended (for UI sync in onResume). */
    fun hasCountdownEnded(): Boolean = countdownActive && endTimeMillis > 0 && System.currentTimeMillis() >= endTimeMillis

    fun getCountdownEndAction(): String = ACTION_COUNTDOWN_END
}
