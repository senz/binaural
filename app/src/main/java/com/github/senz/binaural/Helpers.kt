package com.github.senz.binaural

object Helpers {
    private const val AMPLITUDE_MAX = 32767

    /**
     * Scale amplitude for human perception. 100 Hz or less = max.
     */
    fun getAdjustedAmplitudeMax(frequency: Float): Int =
        if (frequency <= 100) {
            AMPLITUDE_MAX
        } else {
            val amplitudeScale = 100 / frequency
            (AMPLITUDE_MAX * amplitudeScale).toInt()
        }

    fun getLCM(
        a: Int,
        b: Int,
    ): Int {
        val (x, y) = if (a < b) a to b else b to a
        var i = 1
        while (true) {
            val x1 = x * i
            for (j in 1..i) {
                if (x1 == y * j) return x1
            }
            i++
        }
    }

    /**
     * Converts remaining seconds to minutes and seconds for display (e.g. notification, status).
     * @param remainingSeconds total remaining seconds (clamped to >= 0)
     * @return Pair(minutes, seconds)
     */
    fun getRemainingMinutesSeconds(remainingSeconds: Long): Pair<Int, Int> {
        if (remainingSeconds <= 0) return 0 to 0
        val sec = (remainingSeconds % 60).toInt()
        val min = (remainingSeconds / 60).toInt()
        return min to sec
    }
}
