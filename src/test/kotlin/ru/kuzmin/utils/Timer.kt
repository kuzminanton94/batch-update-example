package ru.kuzmin.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

object Timer {

    fun withTimer(runnable: () -> (Unit)): Duration {
        val started = System.nanoTime()
        runnable.invoke()
        val finished = System.nanoTime()
        return (finished - started).nanoseconds
    }
}
