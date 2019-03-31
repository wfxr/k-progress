package com.github.wfxr.kprogress

import java.time.Duration
import java.time.Instant

interface IProgressState {
    val curr: Long
    val total: Long?
    val startInstant: Instant
    val finishedInstant: Instant?

    fun inc() = inc(1)
    fun inc(count: Int) = inc(count.toLong())
    fun inc(count: Long)

    fun finished() = when (val t = total) {
        null -> false
        else -> curr >= t
    }

    fun percentage() = total?.let { curr.toDouble() / it }

    fun timeUsed() = Duration.between(startInstant, finishedInstant ?: Instant.now())!!

    fun speed(unit: Long = 1000) = curr.toDouble() / timeUsed().toMillis() * unit

    fun remain() = total?.let { it - curr }?.let { if (it < 0) 0 else it }

    fun eta() = remain()?.let {
        val speed = speed(1)
        if (speed <= 0.0)
            null
        else
            Duration.ofMillis((it / speed).toLong())
    }

    fun updateTotal(total: Long)

    fun reset(total: Long)

    companion object {
        const val INDEFINITE = Long.MIN_VALUE
    }
}
