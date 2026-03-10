package com.github.senz.binaural

import androidx.test.ext.junit.rules.ActivityScenarioRule
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
    val scenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun appContext_hasCorrectPackage() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(appContext)
        assertEquals("com.github.senz.binaural", appContext.packageName)
    }

    @Test
    fun mainActivity_launches() {
        scenarioRule.scenario.onActivity { activity ->
            assertNotNull(activity)
        }
    }
}
