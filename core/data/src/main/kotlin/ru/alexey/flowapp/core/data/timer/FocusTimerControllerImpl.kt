package ru.alexey.flowapp.core.data.timer

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.FocusRepository
import ru.alexey.flowapp.core.domain.repository.FocusTimerController
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.FocusSession
import ru.alexey.flowapp.core.model.RunningSession
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import ru.alexey.flowapp.core.model.TimerState
import ru.alexey.flowapp.core.model.elapsedAt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

@Single
internal class FocusTimerControllerImpl(
    private val timerStateStore: TimerStateStore,
    private val focusRepository: FocusRepository,
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
) : FocusTimerController {
    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    private val mutex = Mutex()

    override val state: StateFlow<TimerState> = _state.asStateFlow()

    override suspend fun start(session: RunningSession) =
        mutex.withLock {
            val next = TimerState.Running(session = session, startedAt = timeProvider.now())
            _state.value = next
            timerStateStore.write(next)
        }

    override suspend fun pause() =
        mutex.withLock {
            val current = _state.value as? TimerState.Running ?: return@withLock
            val next = TimerState.Paused(
                session = current.session,
                accumulated = current.elapsedAt(timeProvider.now()),
            )
            _state.value = next
            timerStateStore.write(next)
        }

    override suspend fun resume() =
        mutex.withLock {
            val current = _state.value as? TimerState.Paused ?: return@withLock
            val next = TimerState.Running(
                session = current.session,
                startedAt = timeProvider.now(),
                accumulated = current.accumulated,
            )
            _state.value = next
            timerStateStore.write(next)
        }

    override suspend fun stop(completed: Boolean): FocusSession? =
        mutex.withLock {
            finish(completed = completed)
        }

    override suspend fun finishIfElapsed(): FocusSession? =
        mutex.withLock {
            val current = _state.value as? TimerState.Running ?: return@withLock null
            val elapsed = current.elapsedAt(timeProvider.now())
            if (elapsed < current.session.plannedDuration) return@withLock null
            finish(completed = true)
        }

    override suspend fun restore() =
        mutex.withLock {
            _state.value = timerStateStore.read()
        }

    private suspend fun finish(completed: Boolean): FocusSession? =
        withContext(NonCancellable) {
            val current = _state.value
            val session = when (current) {
                TimerState.Idle -> return@withContext null
                is TimerState.Running -> current.session
                is TimerState.Paused -> current.session
            }

            val now = timeProvider.now()
            val elapsed = current.elapsedAt(now).coerceIn(ZERO, session.plannedDuration)

            val focusSession = FocusSession(
                id = session.id,
                taskId = session.taskId,
                categoryId = session.categoryId,
                kind = session.kind,
                plannedDuration = session.plannedDuration,
                actualDuration = elapsed,
                startedAt = session.startedAt,
                endedAt = now,
                completed = completed,
            ).takeIf { elapsed >= 5.seconds }

            if (focusSession != null) {
                focusRepository.saveSession(focusSession)
                if (session.kind == FocusKind.FOCUS) {
                    session.taskId?.let { completeTaskIfDone(it, elapsed) }
                }
            }

            timerStateStore.clear()
            _state.value = TimerState.Idle
            focusSession
        }

    private suspend fun completeTaskIfDone(
        taskId: String,
        elapsed: Duration,
    ) {
        taskRepository.addFocusedTime(taskId, elapsed)
        val task = taskRepository.getTask(taskId) ?: return
        if (task.status == TaskStatus.ACTIVE && task.focusProgress >= 1f) {
            taskRepository.setStatus(taskId, TaskStatus.COMPLETED)
        }
    }
}