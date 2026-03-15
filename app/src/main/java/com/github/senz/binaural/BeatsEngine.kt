package com.github.senz.binaural

/**
 * Callbacks for asynchronous start/stop (after volume ramp).
 * Invoked on the main Looper.
 */
interface BeatsEngineListener {
    fun onStarted(engine: BeatsEngine)

    fun onStopped(engine: BeatsEngine)
}

interface BeatsEngine {
    fun start()

    fun stop()

    fun release()

    val isPlaying: Boolean
}
