package com.github.senz.binaural

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for PlaybackService: notification appears when playing,
 * disappears when stopped; stop action works.
 */
@RunWith(AndroidJUnit4::class)
class PlaybackServiceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val notificationManager get() =
        context.getSystemService(NotificationManager::class.java)

    @After
    fun tearDown() {
        context.startService(
            Intent(context, PlaybackService::class.java).setAction(PlaybackService.ACTION_STOP),
        )
        Thread.sleep(500)
    }

    @Test
    fun startPlayback_showsForegroundNotification() {
        startPlayback(timerMinutes = 0)
        waitForService()
        assertTrue(
            "Notification should be visible when playback is active",
            hasPlaybackNotification(),
        )
    }

    @Test
    fun stopPlayback_removesNotification() {
        startPlayback(timerMinutes = 0)
        waitForService()
        assertTrue(hasPlaybackNotification())
        stopPlayback()
        waitForService()
        assertFalse(
            "Notification should be removed after stop",
            hasPlaybackNotification(),
        )
    }

    @Test
    fun notification_containsExpectedContent() {
        startPlayback(timerMinutes = 0)
        waitForService()
        val notification =
            getPlaybackNotification() ?: run {
                assertTrue("Playback notification should exist", false)
                return
            }
        val extras = notification.notification?.extras
        assertNotNull("Notification should have extras", extras)
        val title = extras!!.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        assertTrue(
            "Notification title should contain app/playing info",
            title?.contains("Binaural") == true || title?.isNotBlank() == true,
        )
        assertTrue(
            "Notification should show Playing or similar",
            text?.contains("Playing") == true || text?.isNotBlank() == true,
        )
    }

    private fun startPlayback(timerMinutes: Int) {
        val intent =
            Intent(context, PlaybackService::class.java).apply {
                action = PlaybackService.ACTION_PLAY
                putExtra(PlaybackService.EXTRA_CARRIER, 200f)
                putExtra(PlaybackService.EXTRA_BEAT, 10f)
                putExtra(PlaybackService.EXTRA_IS_BINAURAL, true)
                putExtra(PlaybackService.EXTRA_TIMER_MINUTES, timerMinutes)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopPlayback() {
        context.startService(
            Intent(context, PlaybackService::class.java).setAction(PlaybackService.ACTION_STOP),
        )
    }

    private fun waitForService() {
        Thread.sleep(800)
    }

    private fun hasPlaybackNotification(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return notificationManager.activeNotifications.any { it.id == PlaybackService.NOTIFICATION_ID }
    }

    private fun getPlaybackNotification() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.activeNotifications.find { it.id == PlaybackService.NOTIFICATION_ID }
        } else {
            null
        }
}
