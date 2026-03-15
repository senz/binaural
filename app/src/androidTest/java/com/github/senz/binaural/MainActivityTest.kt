package com.github.senz.binaural

import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented (integration) tests for MainActivity.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appContext_hasCorrectPackage() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(appContext)
        assertEquals("com.github.senz.binaural", appContext.packageName)
    }

    @Test
    fun mainActivity_launches() {
        composeTestRule.waitForIdle()
        assertNotNull(composeTestRule.activity)
    }

    @Test
    fun startPlayback_viaService_uiShowsPlaying() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val playingText = context.getString(R.string.status_playing)
        composeTestRule.runOnUiThread {
            val playIntent =
                Intent(composeTestRule.activity, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_PLAY
                    putExtra(PlaybackService.EXTRA_CARRIER, 200f)
                    putExtra(PlaybackService.EXTRA_BEAT, 10f)
                    putExtra(PlaybackService.EXTRA_IS_BINAURAL, true)
                    putExtra(PlaybackService.EXTRA_TIMER_MINUTES, 0)
                }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                composeTestRule.activity.startForegroundService(playIntent)
            } else {
                composeTestRule.activity.startService(playIntent)
            }
        }
        Thread.sleep(1200)
        composeTestRule.onNodeWithText(playingText).assertExists()
        composeTestRule.runOnUiThread {
            composeTestRule.activity.startService(
                Intent(composeTestRule.activity, PlaybackService::class.java)
                    .setAction(PlaybackService.ACTION_STOP),
            )
        }
    }
}
