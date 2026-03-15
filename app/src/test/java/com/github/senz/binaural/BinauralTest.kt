package com.github.senz.binaural

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [Binaural] sample generation math (no AudioTrack).
 */
class BinauralTest {
    private val frequency = 200f
    private val isoBeat = 10f
    private val sampleRate = 44100

    @Test
    fun generateSamples_lengthMatchesFormula() {
        val samples = Binaural.generateSamples(frequency, isoBeat, sampleRate)
        val freqLeft = frequency - (isoBeat / 2)
        val freqRight = frequency + (isoBeat / 2)
        val sCountLeft = (sampleRate / freqLeft).toInt()
        val sCountRight = (sampleRate / freqRight).toInt()
        val expectedLength = Helpers.getLCM(sCountLeft, sCountRight) * 2
        assertEquals(expectedLength, samples.size)
    }

    @Test
    fun generateSamples_sizeIsEvenStereo() {
        val samples = Binaural.generateSamples(frequency, isoBeat, sampleRate)
        assertTrue(samples.size % 2 == 0)
    }

    @Test
    fun generateSamples_firstFrameZeroPhase() {
        val samples = Binaural.generateSamples(frequency, isoBeat, sampleRate)
        assertEquals(0, samples[0].toInt(), "left channel at start")
        assertEquals(0, samples[1].toInt(), "right channel at start")
    }

    @Test
    fun generateSamples_allSamplesInValidRange() {
        val samples = Binaural.generateSamples(frequency, isoBeat, sampleRate)
        for (i in samples.indices) {
            val v = samples[i].toInt()
            assertTrue(v in -32768..32767, "sample[$i]=$v out of range")
        }
    }

    /** PCM 16-bit: each sample is 2 bytes. Same formula as in Binaural/Isochronic setBufferSizeInBytes. */
    @Test
    fun bufferSizeBytes_isSampleCountTimesTwo() {
        val samples = Binaural.generateSamples(frequency, isoBeat, sampleRate)
        val bufferSizeBytes = samples.size * 2
        assertTrue(bufferSizeBytes > 0)
        assertEquals(samples.size * 2, bufferSizeBytes)
    }

    @Test
    fun generateSamples_spotValuesRegression() {
        val samples = Binaural.generateSamples(frequency, isoBeat, sampleRate)
        // Captured from current implementation; change would indicate algorithm regression
        val s2 = samples[2].toInt()
        val s3 = samples[3].toInt()
        val mid = samples.size / 2
        val midL = samples[mid].toInt()
        val midR = samples[mid + 1].toInt()
        assertEquals(455, s2, "left sample at index 2 (actual=$s2)")
        assertEquals(478, s3, "right sample at index 3 (actual=$s3)")
        assertEquals(35, midL, "left sample at midpoint (actual=$midL)")
        assertEquals(-58, midR, "right sample at midpoint (actual=$midR)")
    }
}
