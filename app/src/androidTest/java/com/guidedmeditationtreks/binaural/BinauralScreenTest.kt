package com.guidedmeditationtreks.binaural

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for BinauralScreen.
 */
@RunWith(AndroidJUnit4::class)
class BinauralScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun timerDropdown_closesWhenPlayClicked() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val noTimer = context.getString(R.string.timer_no)
        val tenMin = context.getString(R.string.timer_10)
        val playDesc = "Play"

        // Expand timer dropdown by clicking the timer field (shows "No timer")
        composeTestRule.onNodeWithText(noTimer).performClick()
        composeTestRule.waitForIdle()
        // Dropdown is open: menu item "10 min" is in the tree
        composeTestRule.onNodeWithText(tenMin).assertExists()

        // Start playback: Play button click must close the dropdown
        composeTestRule.onNodeWithContentDescription(playDesc).performClick()
        composeTestRule.waitForIdle()
        // Dropdown must be closed: menu item "10 min" is no longer in the tree
        composeTestRule.onNodeWithText(tenMin).assertDoesNotExist()
    }
}
