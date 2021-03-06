/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.github.wfxr.kprogress

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProgressStateTest {
    @Test
    fun testInitStateIndefinite() {
        val ps = ProgressState()
        assertEquals(null, ps.total)
        assertEquals(0, ps.curr)
        assertTrue { ps.startInstant <= Instant.now() }
        assertFalse { ps.finished() }
    }

    @Test
    fun testInitState() {
        val ps = ProgressState(0)
        assertEquals(0, ps.total)
        assertEquals(0, ps.curr)
        assertTrue { ps.startInstant <= Instant.now() }
        assertTrue { ps.finished() }
    }

    @Test
    fun testInc() {
        val ps = ProgressState()
        assertEquals(0, ps.curr)

        ps.inc()
        assertEquals(1, ps.curr)

        ps.inc(5)
        assertEquals(6, ps.curr)
    }

    @Test
    fun testFinished() {
        run {
            val ps = ProgressState()
            assertFalse { ps.finished() }
            ps.inc()
            assertFalse { ps.finished() }
            ps.inc(100000)
            assertFalse { ps.finished() }
        }
        run {
            val ps = ProgressState(100)
            assertFalse { ps.finished() }
            ps.inc()
            assertFalse { ps.finished() }
            ps.inc(99)
            assertTrue { ps.finished() }
        }
    }

    @Test
    fun testReset() {
        run {
            val ps = ProgressState(500)
            ps.inc(500)
            val oldStartInstant = ps.startInstant
            assertTrue { ps.finished() }

            ps.reset(100)
            Thread.sleep(10)

            assertEquals(100, ps.total)
            assertEquals(0, ps.curr)
            assertTrue { ps.startInstant > oldStartInstant }
            assertFalse { ps.finished() }
        }
    }
}
