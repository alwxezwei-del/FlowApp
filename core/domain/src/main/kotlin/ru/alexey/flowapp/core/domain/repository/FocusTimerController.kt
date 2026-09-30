package ru.alexey.flowapp.core.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.alexey.flowapp.core.model.FocusSession
import ru.alexey.flowapp.core.model.RunningSession
import ru.alexey.flowapp.core.model.TimerState

/**
 * Owns the active focus session state, shared by the screen, service and notification
 */
interface FocusTimerController {
    val state: StateFlow<TimerState>

    suspend fun start(session: RunningSession)

    suspend fun pause()

    suspend fun resume()

    /**
     * Stops the timer and saves the session
     */
    suspend fun stop(completed: Boolean): FocusSession?

    /**
     * Finishes the interval if its time has already elapsed
     */
    suspend fun finishIfElapsed(): FocusSession?

    /** Restores state after process restart */
    suspend fun restore()
}