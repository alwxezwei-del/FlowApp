package ru.alexey.flowapp.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** Timer math tests. State is timestamp-based, so everything is pure functions. */
class TimerStateTest {
    private val start = Instant.fromEpochMilliseconds(1_000_000)
    private val session = RunningSession(
        id = "session",
        plannedDuration = 25.minutes,
        startedAt = start,
    )

    @Test
    fun `idle timer has no elapsed time`() {
        assertEquals(ZERO, TimerState.Idle.elapsedAt(start + 10.minutes))
    }

    @Test
    fun `running timer counts time since it was resumed`() {
        val state = TimerState.Running(session = session, startedAt = start)

        assertEquals(10.minutes, state.elapsedAt(start + 10.minutes))
    }

    @Test
    fun `running timer adds time accumulated before the pause`() {
        val state = TimerState.Running(
            session = session,
            startedAt = start + 5.minutes,
            accumulated = 3.minutes,
        )

        assertEquals(5.minutes, state.elapsedAt(start + 7.minutes))
    }

    @Test
    fun `paused timer does not advance`() {
        val state = TimerState.Paused(session = session, accumulated = 8.minutes)

        assertEquals(8.minutes, state.elapsedAt(start + 30.minutes))
    }

    @Test
    fun `remaining time never goes below zero`() {
        val state = TimerState.Running(session = session, startedAt = start)

        assertEquals(ZERO, state.remainingAt(start + 40.minutes))
    }

    @Test
    fun `remaining time counts down from the planned duration`() {
        val state = TimerState.Running(session = session, startedAt = start)

        assertEquals(20.minutes, state.remainingAt(start + 5.minutes))
    }

    @Test
    fun `progress is capped at one when the interval is over`() {
        val state = TimerState.Running(session = session, startedAt = start)

        assertEquals(1f, state.progressAt(start + 60.minutes), TOLERANCE)
    }

    @Test
    fun `progress is a fraction of the planned duration`() {
        val state = TimerState.Running(session = session, startedAt = start)

        assertEquals(0.2f, state.progressAt(start + 5.minutes), TOLERANCE)
    }

    @Test
    fun `progress of an idle timer is zero`() {
        assertEquals(0f, TimerState.Idle.progressAt(start), TOLERANCE)
    }

    @Test
    fun `elapsed time survives a process restart because it is derived from the clock`() {
        // Process died 30s after start and was restored 10 min later:
        // state didn't tick, but elapsed time is still correct
        val restored = TimerState.Running(session = session, startedAt = start, accumulated = ZERO)

        assertEquals(10.minutes + 30.seconds, restored.elapsedAt(start + 10.minutes + 30.seconds))
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}