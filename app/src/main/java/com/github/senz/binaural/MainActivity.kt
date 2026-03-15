package com.github.senz.binaural

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

private const val PREF_NAME = "binaural_prefs"
private const val KEY_NOTIFICATION_PROMPT_SEEN = "notification_prompt_seen"

class MainActivity : ComponentActivity() {
    private var isPlaying by mutableStateOf(false)
        private set
    private var timerEndTimeMillis by mutableStateOf(0L)
        private set
    private var showNotificationPrompt by mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            markNotificationPromptSeen()
            showNotificationPrompt = false
        }

    private val playbackReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                when (intent?.action) {
                    PlaybackService.PLAYBACK_STARTED -> {
                        isPlaying = true
                        timerEndTimeMillis = intent.getLongExtra(PlaybackService.KEY_END_TIME_MILLIS, 0L)
                    }
                    PlaybackService.PLAYBACK_STOPPED -> {
                        isPlaying = false
                        timerEndTimeMillis = 0L
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (!getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_NOTIFICATION_PROMPT_SEEN, false) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            showNotificationPrompt = true
        }
        setContent {
            BinauralTheme {
                Box {
                    BinauralScreen(
                        isPlaying = isPlaying,
                        timerEndTimeMillis = timerEndTimeMillis,
                        onPlayRequested = { carrier, beat, isBinaural, timerMinutes ->
                            startPlayback(carrier, beat, isBinaural, timerMinutes)
                        },
                        onStopRequested = { stopPlayback() },
                        onRefreshAndRestart = { carrier, beat, isBinaural, timerMinutes ->
                            if (isPlaying) startPlayback(carrier, beat, isBinaural, timerMinutes)
                        },
                    )
                    if (showNotificationPrompt) {
                        NotificationPermissionDialog(
                            onEnable = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(
                                            this@MainActivity,
                                            android.Manifest.permission.POST_NOTIFICATIONS,
                                        ) ==
                                        PackageManager.PERMISSION_GRANTED
                                    ) {
                                        markNotificationPromptSeen()
                                        showNotificationPrompt = false
                                    } else {
                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    markNotificationPromptSeen()
                                    showNotificationPrompt = false
                                }
                            },
                            onSkip = {
                                markNotificationPromptSeen()
                                showNotificationPrompt = false
                            },
                        )
                    }
                }
            }
        }
    }

    private fun markNotificationPromptSeen() {
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFICATION_PROMPT_SEEN, true)
            .apply()
    }

    override fun onStart() {
        super.onStart()
        val filter =
            IntentFilter().apply {
                addAction(PlaybackService.PLAYBACK_STARTED)
                addAction(PlaybackService.PLAYBACK_STOPPED)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playbackReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(playbackReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(playbackReceiver)
    }

    override fun onResume() {
        super.onResume()
        if (CountdownHelper.getInstance().hasCountdownEnded()) {
            CountdownHelper.getInstance().stopFromAlarm()
            isPlaying = false
            timerEndTimeMillis = 0L
        } else if (isPlaying && !isPlaybackServiceRunning()) {
            isPlaying = false
            timerEndTimeMillis = 0L
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startPlayback(
        carrier: Float,
        beat: Float,
        isBinaural: Boolean,
        timerMinutes: Int?,
    ) {
        val intent =
            Intent(this, PlaybackService::class.java).apply {
                action = PlaybackService.ACTION_PLAY
                putExtra(PlaybackService.EXTRA_CARRIER, carrier)
                putExtra(PlaybackService.EXTRA_BEAT, beat)
                putExtra(PlaybackService.EXTRA_IS_BINAURAL, isBinaural)
                putExtra(PlaybackService.EXTRA_TIMER_MINUTES, timerMinutes ?: 0)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopPlayback() {
        startService(Intent(this, PlaybackService::class.java).setAction(PlaybackService.ACTION_STOP))
    }

    @Suppress("DEPRECATION")
    private fun isPlaybackServiceRunning(): Boolean =
        (getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager)
            .getRunningServices(Int.MAX_VALUE)
            ?.any { it.service.className == PlaybackService::class.java.name } == true
}
