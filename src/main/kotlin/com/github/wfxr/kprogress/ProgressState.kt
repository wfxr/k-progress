package com.github.wfxr.kprogress

import com.github.wfxr.kprogress.IProgressState.Companion.INDEFINITE
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class ProgressState(total: Long = INDEFINITE) : IProgressState {
    private val mTotal: AtomicLong = AtomicLong(total)
    private val mCurr = AtomicLong(0)
    private var mStartInstant = Instant.now()!!
    private var mFinishedInstant: Instant? = null
    override val curr get() = mCurr.get()
    override val startInstant get() = synchronized(mStartInstant) { mStartInstant }
    override val total
        get() = when (val value = mTotal.get()) {
            INDEFINITE -> null
            else       -> value
        }
    override val finishedInstant: Instant?
        get() = mFinishedInstant

    override fun updateTotal(total: Long) {
        mTotal.set(total)
        if (curr < total) mFinishedInstant = null
    }

    override fun inc(count: Long) {
        val curr = mCurr.addAndGet(count)
        total?.let { if (curr >= it) mFinishedInstant = Instant.now() }
    }

    override fun reset(total: Long) {
        mCurr.set(0)
        mTotal.set(total)
        mStartInstant = Instant.now()
        mFinishedInstant = null
    }
}
