package com.github.senz.binaural

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [Isochronic] sample generation math (no AudioTrack).
 */
class IsochronicTest {
    private val frequency = 200f
    private val isoBeat = 10f
    private val sampleRate = 44100

    @Test
    fun generateSamples_lengthMatchesFormula() {
        val samples = Isochronic.generateSamples(frequency, isoBeat, sampleRate)
        val multiplier = (sampleRate / 2 / isoBeat).toInt()
        val expectedLength = multiplier * 2
        assertEquals(expectedLength, samples.size)
    }

    @Test
    fun generateSamples_allSamplesInValidRange() {
        val samples = Isochronic.generateSamples(frequency, isoBeat, sampleRate)
        for (i in samples.indices) {
            val v = samples[i].toInt()
            assertTrue(v in -32768..32767, "sample[$i]=$v out of range")
        }
    }

    @Test
    fun generateSamples_spotValuesRegression() {
        val samples = Isochronic.generateSamples(frequency, isoBeat, sampleRate)
        // Captured from current implementation; change would indicate algorithm regression
        val s0 = samples[0].toInt()
        val s1 = samples[1].toInt()
        val mid = samples.size / 2
        val midVal = samples[mid].toInt()
        assertEquals(0, s0, "sample at start (actual=$s0)")
        assertEquals(14, s1, "sample at index 1 (actual=$s1)")
        assertEquals(2289, midVal, "sample at midpoint (actual=$midVal)")
    }

    @Test
    fun generateSamples_highBeatProducesShortPeriod() {
        val highBeat = 100f
        val samples = Isochronic.generateSamples(frequency, highBeat, sampleRate)
        val multiplier = (sampleRate / 2 / highBeat).toInt()
        assertEquals(multiplier * 2, samples.size)
        assertTrue(samples.size <= 500, "high isoBeat should yield short period for buffer expansion at runtime")
    }
}
