package ru.alexey.flowapp.core.data.timer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.RunningSession
import ru.alexey.flowapp.core.model.TimerState
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val Context.timerDataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_state")

@Single
class TimerStateStore(
    private val context: Context,
) {
    private val dataStore get() = context.timerDataStore

    suspend fun read(): TimerState {
        val prefs = runCatching { dataStore.data.first() }.getOrNull() ?: return TimerState.Idle
        val sessionId = prefs[KeySessionId] ?: return TimerState.Idle
        val plannedSeconds = prefs[KeyPlannedSeconds] ?: return TimerState.Idle

        val session = RunningSession(
            id = sessionId,
            taskId = prefs[KeyTaskId],
            taskTitle = prefs[KeyTaskTitle],
            categoryId = prefs[KeyCategoryId],
            kind = FocusKind.parse(prefs[KeyKind]),
            plannedDuration = plannedSeconds.seconds,
            startedAt = Instant.fromEpochMilliseconds(prefs[KeySessionStartedAt] ?: 0L),
        )
        val accumulated = (prefs[KeyAccumulatedSeconds] ?: 0L).seconds

        return if (prefs[KeyRunning] == true) {
            TimerState.Running(
                session = session,
                startedAt = Instant.fromEpochMilliseconds(prefs[KeyResumedAt] ?: 0L),
                accumulated = accumulated,
            )
        } else {
            TimerState.Paused(session = session, accumulated = accumulated)
        }
    }

    suspend fun write(state: TimerState) {
        if (state is TimerState.Idle) {
            clear()
            return
        }

        val session = when (state) {
            is TimerState.Running -> state.session
            is TimerState.Paused -> state.session
            TimerState.Idle -> return
        }

        dataStore.edit { prefs ->
            prefs[KeySessionId] = session.id
            session.taskId?.let { prefs[KeyTaskId] = it } ?: prefs.remove(KeyTaskId)
            session.taskTitle?.let { prefs[KeyTaskTitle] = it } ?: prefs.remove(KeyTaskTitle)
            session.categoryId?.let { prefs[KeyCategoryId] = it } ?: prefs.remove(KeyCategoryId)
            prefs[KeyKind] = session.kind.key
            prefs[KeyPlannedSeconds] = session.plannedDuration.inWholeSeconds
            prefs[KeySessionStartedAt] = session.startedAt.toEpochMilliseconds()

            when (state) {
                is TimerState.Running -> {
                    prefs[KeyRunning] = true
                    prefs[KeyResumedAt] = state.startedAt.toEpochMilliseconds()
                    prefs[KeyAccumulatedSeconds] = state.accumulated.inWholeSeconds
                }

                is TimerState.Paused -> {
                    prefs[KeyRunning] = false
                    prefs[KeyAccumulatedSeconds] = state.accumulated.inWholeSeconds
                }

                TimerState.Idle -> {
                    Unit
                }
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val KeySessionId = stringPreferencesKey("session_id")
        val KeyTaskId = stringPreferencesKey("task_id")
        val KeyTaskTitle = stringPreferencesKey("task_title")
        val KeyCategoryId = stringPreferencesKey("category_id")
        val KeyKind = stringPreferencesKey("kind")
        val KeyPlannedSeconds = longPreferencesKey("planned_seconds")
        val KeyAccumulatedSeconds = longPreferencesKey("accumulated_seconds")
        val KeySessionStartedAt = longPreferencesKey("session_started_at")
        val KeyResumedAt = longPreferencesKey("resumed_at")
        val KeyRunning = booleanPreferencesKey("running")
    }
}