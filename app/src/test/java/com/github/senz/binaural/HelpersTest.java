package com.github.senz.binaural;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link Helpers}.
 */
public class HelpersTest {

    @Test
    public void getAdjustedAmplitudeMax_frequency100OrLess_returnsMaxAmplitude() {
        assertEquals(32767, Helpers.getAdjustedAmplitudeMax(100f));
        assertEquals(32767, Helpers.getAdjustedAmplitudeMax(50f));
        assertEquals(32767, Helpers.getAdjustedAmplitudeMax(1f));
    }

    @Test
    public void getAdjustedAmplitudeMax_frequencyAbove100_scalesDown() {
        // 100/200 = 0.5 -> 32767 * 0.5 = 16383
        assertEquals(16383, Helpers.getAdjustedAmplitudeMax(200f));
        // 100/1000 = 0.1 -> 3276
        assertEquals(3276, Helpers.getAdjustedAmplitudeMax(1000f));
    }

    @Test
    public void getLCM_simplePairs_returnsCorrectLCM() {
        assertEquals(6, Helpers.getLCM(2, 3));
        assertEquals(12, Helpers.getLCM(4, 6));
        assertEquals(15, Helpers.getLCM(3, 5));
        assertEquals(1, Helpers.getLCM(1, 1));
    }

    @Test
    public void getLCM_orderIndependent_returnsSameResult() {
        assertEquals(Helpers.getLCM(4, 6), Helpers.getLCM(6, 4));
        assertEquals(Helpers.getLCM(12, 18), Helpers.getLCM(18, 12));
    }

    @Test
    public void getLCM_equalNumbers_returnsNumber() {
        assertEquals(7, Helpers.getLCM(7, 7));
        assertEquals(10, Helpers.getLCM(10, 10));
    }

    @Test
    public void getRemainingMinutesSeconds_zeroOrNegative_returnsZeroZero() {
        assertArrayEquals(new int[] { 0, 0 }, Helpers.getRemainingMinutesSeconds(0));
        assertArrayEquals(new int[] { 0, 0 }, Helpers.getRemainingMinutesSeconds(-1));
        assertArrayEquals(new int[] { 0, 0 }, Helpers.getRemainingMinutesSeconds(-60));
    }

    @Test
    public void getRemainingMinutesSeconds_underOneMinute_returnsZeroAndSeconds() {
        assertArrayEquals(new int[] { 0, 1 }, Helpers.getRemainingMinutesSeconds(1));
        assertArrayEquals(new int[] { 0, 59 }, Helpers.getRemainingMinutesSeconds(59));
    }

    @Test
    public void getRemainingMinutesSeconds_oneMinute_returnsOneZero() {
        assertArrayEquals(new int[] { 1, 0 }, Helpers.getRemainingMinutesSeconds(60));
    }

    @Test
    public void getRemainingMinutesSeconds_minutesAndSeconds_returnsCorrectPair() {
        assertArrayEquals(new int[] { 1, 30 }, Helpers.getRemainingMinutesSeconds(90));
        assertArrayEquals(new int[] { 2, 5 }, Helpers.getRemainingMinutesSeconds(125));
        assertArrayEquals(new int[] { 5, 0 }, Helpers.getRemainingMinutesSeconds(300));
        assertArrayEquals(new int[] { 10, 15 }, Helpers.getRemainingMinutesSeconds(615));
    }
}
