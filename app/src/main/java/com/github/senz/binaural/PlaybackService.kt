package com.github.senz.binaural

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

class PlaybackService : Service() {
    private var wave: BeatsEngine? = null
    private var endTimeMillis: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_PLAY -> handlePlay(intent)
            ACTION_STOP -> handleStop()
            else -> { /* no-op */ }
        }
        return START_NOT_STICKY
    }

    private fun handlePlay(intent: Intent) {
        CountdownHelper.cancelCountdown(this)
        wave?.release()
        wave = null

        val carrier = intent.getFloatExtra(EXTRA_CARRIER, 200f)
        val beat = intent.getFloatExtra(EXTRA_BEAT, 10f)
        val isBinaural = intent.getBooleanExtra(EXTRA_IS_BINAURAL, true)
        val timerMinutes = intent.getIntExtra(EXTRA_TIMER_MINUTES, 0).takeIf { it > 0 }
        val endTimeFromIntent = intent.getLongExtra(EXTRA_END_TIME_MILLIS, 0L)

        wave = if (isBinaural) Binaural(carrier, beat, null) else Isochronic(carrier, beat, null)
        wave?.start() ?: return

        endTimeMillis =
            if (endTimeFromIntent > 0L) {
                endTimeFromIntent
            } else if (timerMinutes != null) {
                System.currentTimeMillis() + timerMinutes * 60_000L
            } else {
                0L
            }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            stopPlaybackAndNotify()
            return
        }

        createNotificationChannel()
        val notification = buildNotification(endTimeMillis)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "startForeground failed (missing permission?)", e)
            stopPlaybackAndNotify()
            return
        }

        if (endTimeMillis > 0) {
            CountdownHelper.startCountdown(wave!!, endTimeMillis, this)
            scheduleNotificationUpdates(endTimeMillis)
        }

        sendBroadcast(
            Intent(PLAYBACK_STARTED).apply {
                setPackage(packageName)
                putExtra(KEY_END_TIME_MILLIS, endTimeMillis)
            },
        )
    }

    private fun scheduleNotificationUpdates(endTime: Long) {
        updateRunnable?.let { handler.removeCallbacks(it) }
        val runnable =
            object : Runnable {
                override fun run() {
                    if (endTime <= 0) return
                    val now = System.currentTimeMillis()
                    if (now >= endTime) {
                        // Timer reached: stop playback even if AlarmManager is inexact/delayed.
                        updateRunnable = null
                        handleStop()
                        return
                    }
                    val notification = buildNotification(endTime)
                    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                        .notify(NOTIFICATION_ID, notification)
                    handler.postDelayed(this, 1000L)
                }
            }
        updateRunnable = runnable
        handler.postDelayed(runnable, 1000L)
    }

    /**
     * Stops playback and service when we cannot run as foreground (e.g. missing POST_NOTIFICATIONS).
     * Does not call stopForeground() since we never called startForeground().
     */
    private fun stopPlaybackAndNotify() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        CountdownHelper.cancelCountdown(this)
        wave?.stop()
        wave?.release()
        wave = null
        endTimeMillis = 0L
        stopSelf()
        sendBroadcast(Intent(PLAYBACK_STOPPED).setPackage(packageName))
    }

    private fun handleStop() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        CountdownHelper.cancelCountdown(this)
        wave?.stop()
        wave?.release()
        wave = null
        endTimeMillis = 0L
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
        sendBroadcast(Intent(PLAYBACK_STOPPED).setPackage(packageName))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(endTime: Long): Notification {
        val contentText =
            if (endTime > 0) {
                val remaining = (endTime - System.currentTimeMillis()) / 1000
                val (min, sec) = Helpers.getRemainingMinutesSeconds(remaining)
                getString(R.string.status_remaining, min, sec)
            } else {
                getString(R.string.notification_playing)
            }

        val stopIntent = Intent(this, PlaybackService::class.java).setAction(ACTION_STOP)
        val stopPendingIntent =
            PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_playing_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification_playback)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_notification_playback,
                getString(R.string.notification_stop),
                stopPendingIntent,
            ).setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val TAG = "PlaybackService"
        const val ACTION_PLAY = "com.github.senz.binaural.ACTION_PLAY"
        const val ACTION_STOP = "com.github.senz.binaural.ACTION_STOP"
        const val EXTRA_CARRIER = "com.github.senz.binaural.EXTRA_CARRIER"
        const val EXTRA_BEAT = "com.github.senz.binaural.EXTRA_BEAT"
        const val EXTRA_IS_BINAURAL = "com.github.senz.binaural.EXTRA_IS_BINAURAL"
        const val EXTRA_TIMER_MINUTES = "com.github.senz.binaural.EXTRA_TIMER_MINUTES"

        /** Optional: if set, used as end time (e.g. for tests) instead of computing from timer minutes. */
        const val EXTRA_END_TIME_MILLIS = "com.github.senz.binaural.EXTRA_END_TIME_MILLIS"
        const val PLAYBACK_STARTED = "com.github.senz.binaural.PLAYBACK_STARTED"
        const val PLAYBACK_STOPPED = "com.github.senz.binaural.PLAYBACK_STOPPED"
        const val KEY_END_TIME_MILLIS = "com.github.senz.binaural.KEY_END_TIME_MILLIS"
        const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "binaural_playback"
    }
}
