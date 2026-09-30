package ru.alexey.flowapp.core.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Instant

/**
 * Focus timer state, the single source of truth for the screen and notification
 *
 * Stored as [startedAt] + [accumulated] rather than remaining seconds, so it can be restored after
 * process death just by reading the clock.
 */
sealed interface TimerState {
    data object Idle : TimerState

    /**
     * Timer running
     *
     * @param startedAt last start or resume time
     * @param accumulated time accumulated before the last pause
     */
    data class Running(
        val session: RunningSession,
        val startedAt: Instant,
        val accumulated: Duration = ZERO,
    ) : TimerState

    /**
     * Timer paused
     *
     * @param accumulated time accumulated at pause
     */
    data class Paused(
        val session: RunningSession,
        val accumulated: Duration,
    ) : TimerState
}

/**
 * Immutable part of a running session
 *
 * @param id id of the resulting [FocusSession]
 * @param taskTitle for the notification, so the service does not need DB access
 * @param startedAt first start time
 */
data class RunningSession(
    val id: String = newId(),
    val taskId: String? = null,
    val taskTitle: String? = null,
    val categoryId: String? = null,
    val kind: FocusKind = FocusKind.FOCUS,
    val plannedDuration: Duration,
    val startedAt: Instant,
)

/** Session if the timer is running or paused */
val TimerState.runningSession: RunningSession?
    get() = when (this) {
        TimerState.Idle -> null
        is TimerState.Running -> session
        is TimerState.Paused -> session
    }

fun TimerState.elapsedAt(now: Instant): Duration =
    when (this) {
        TimerState.Idle -> ZERO
        is TimerState.Paused -> accumulated
        is TimerState.Running -> accumulated + (now - startedAt)
    }

fun TimerState.remainingAt(now: Instant): Duration {
    val session = runningSession ?: return ZERO
    return (session.plannedDuration - elapsedAt(now)).coerceAtLeast(ZERO)
}

fun TimerState.progressAt(now: Instant): Float {
    val session = runningSession ?: return 0f
    if (session.plannedDuration <= ZERO) return 0f
    return (elapsedAt(now) / session.plannedDuration).toFloat().coerceIn(0f, 1f)
}