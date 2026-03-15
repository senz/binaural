package com.github.senz.binaural

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [Helpers].
 */
class HelpersTest {
    @Test
    fun getAdjustedAmplitudeMax_frequency100OrLess_returnsMaxAmplitude() {
        assertEquals(32767, Helpers.getAdjustedAmplitudeMax(100f))
        assertEquals(32767, Helpers.getAdjustedAmplitudeMax(50f))
        assertEquals(32767, Helpers.getAdjustedAmplitudeMax(1f))
    }

    @Test
    fun getAdjustedAmplitudeMax_frequencyAbove100_scalesDown() {
        // 100/200 = 0.5 -> 32767 * 0.5 = 16383
        assertEquals(16383, Helpers.getAdjustedAmplitudeMax(200f))
        // 100/1000 = 0.1 -> 3276
        assertEquals(3276, Helpers.getAdjustedAmplitudeMax(1000f))
    }

    @Test
    fun getLCM_simplePairs_returnsCorrectLCM() {
        assertEquals(6, Helpers.getLCM(2, 3))
        assertEquals(12, Helpers.getLCM(4, 6))
        assertEquals(15, Helpers.getLCM(3, 5))
        assertEquals(1, Helpers.getLCM(1, 1))
    }

    @Test
    fun getLCM_orderIndependent_returnsSameResult() {
        assertEquals(Helpers.getLCM(4, 6), Helpers.getLCM(6, 4))
        assertEquals(Helpers.getLCM(12, 18), Helpers.getLCM(18, 12))
    }

    @Test
    fun getLCM_equalNumbers_returnsNumber() {
        assertEquals(7, Helpers.getLCM(7, 7))
        assertEquals(10, Helpers.getLCM(10, 10))
    }

    @Test
    fun getRemainingMinutesSeconds_zeroOrNegative_returnsZeroZero() {
        assertEquals(0 to 0, Helpers.getRemainingMinutesSeconds(0))
        assertEquals(0 to 0, Helpers.getRemainingMinutesSeconds(-1))
        assertEquals(0 to 0, Helpers.getRemainingMinutesSeconds(-60))
    }

    @Test
    fun getRemainingMinutesSeconds_underOneMinute_returnsZeroAndSeconds() {
        assertEquals(0 to 1, Helpers.getRemainingMinutesSeconds(1))
        assertEquals(0 to 59, Helpers.getRemainingMinutesSeconds(59))
    }

    @Test
    fun getRemainingMinutesSeconds_oneMinute_returnsOneZero() {
        assertEquals(1 to 0, Helpers.getRemainingMinutesSeconds(60))
    }

    @Test
    fun getRemainingMinutesSeconds_minutesAndSeconds_returnsCorrectPair() {
        assertEquals(1 to 30, Helpers.getRemainingMinutesSeconds(90))
        assertEquals(2 to 5, Helpers.getRemainingMinutesSeconds(125))
        assertEquals(5 to 0, Helpers.getRemainingMinutesSeconds(300))
        assertEquals(10 to 15, Helpers.getRemainingMinutesSeconds(615))
    }
}
