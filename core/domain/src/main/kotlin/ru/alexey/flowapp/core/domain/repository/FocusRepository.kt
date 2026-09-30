package ru.alexey.flowapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.FocusSession
import kotlin.time.Duration

/** Completed focus sessions */
interface FocusRepository {
    /** Sessions in range, newest first */
    fun observeSessions(range: DateRange): Flow<List<FocusSession>>

    /** Latest sessions for the history feed */
    fun observeRecentSessions(limit: Int): Flow<List<FocusSession>>

    /** Total focused time in range */
    fun observeTotalFocus(range: DateRange): Flow<Duration>

    suspend fun saveSession(session: FocusSession)

    suspend fun deleteSession(id: String)
}