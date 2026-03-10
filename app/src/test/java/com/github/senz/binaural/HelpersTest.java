package com.github.senz.binaural;

import org.junit.Test;
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
}
