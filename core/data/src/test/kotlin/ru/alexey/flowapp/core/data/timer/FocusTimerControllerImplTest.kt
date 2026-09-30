package ru.alexey.flowapp.core.data.timer

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.FocusRepository
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.FocusSession
import ru.alexey.flowapp.core.model.RunningSession
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import ru.alexey.flowapp.core.model.TimerState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class FocusTimerControllerImplTest {
    private val clock = FakeClock()
    private val timerStateStore = mockk<TimerStateStore>(relaxed = true)
    private val focusRepository = mockk<FocusRepository>(relaxed = true)
    private val taskRepository = mockk<TaskRepository>(relaxed = true)

    private val controller = FocusTimerControllerImpl(timerStateStore, focusRepository, taskRepository, clock)

    private val task = Task(id = "task", title = "Write report", estimate = 25.minutes, createdAt = Instant.fromEpochMilliseconds(0))

    @Test
    fun `stop saves elapsed time excluding pauses`() =
        runTest {
            controller.start(session(taskId = null))
            clock.advance(10.minutes)
            controller.pause()
            clock.advance(30.minutes)
            controller.resume()
            clock.advance(5.minutes)

            val saved = controller.stop(completed = false)

            assertEquals(15.minutes, saved?.actualDuration)
            assertEquals(TimerState.Idle, controller.state.value)
            coVerify { focusRepository.saveSession(saved!!) }
            coVerify { timerStateStore.clear() }
        }

    @Test
    fun `session shorter than minimum is not saved`() =
        runTest {
            controller.start(session(taskId = null))
            clock.advance(3.seconds)

            assertNull(controller.stop(completed = false))
            assertEquals(TimerState.Idle, controller.state.value)
            coVerify(exactly = 0) { focusRepository.saveSession(any()) }
            coVerify { timerStateStore.clear() }
        }

    @Test
    fun `finishIfElapsed does nothing before planned duration`() =
        runTest {
            controller.start(session(taskId = null))
            clock.advance(24.minutes)

            assertNull(controller.finishIfElapsed())
            assertTrue(controller.state.value is TimerState.Running)
        }

    @Test
    fun `finishIfElapsed caps duration and completes task when estimate is reached`() =
        runTest {
            coEvery { taskRepository.getTask("task") } returns task andThen task.copy(focusedTime = 25.minutes)
            controller.start(session(taskId = "task"))
            clock.advance(40.minutes)

            val saved = controller.finishIfElapsed()

            assertEquals(25.minutes, saved?.actualDuration)
            assertEquals(true, saved?.completed)
            coVerify { taskRepository.addFocusedTime("task", 25.minutes) }
            coVerify { taskRepository.setStatus("task", TaskStatus.COMPLETED) }
        }

    @Test
    fun `session of a deleted task is saved without task`() =
        runTest {
            coEvery { taskRepository.getTask("task") } returns null
            val saved = slot<FocusSession>()
            controller.start(session(taskId = "task"))
            clock.advance(10.minutes)

            controller.stop(completed = false)

            coVerify { focusRepository.saveSession(capture(saved)) }
            assertNull(saved.captured.taskId)
            coVerify(exactly = 0) { taskRepository.addFocusedTime(any(), any()) }
        }

    @Test
    fun `failed save still leaves timer idle`() =
        runTest {
            coEvery { focusRepository.saveSession(any()) } throws IllegalStateException("db")
            controller.start(session(taskId = null))
            clock.advance(10.minutes)

            runCatching { controller.stop(completed = false) }

            assertEquals(TimerState.Idle, controller.state.value)
            coVerify { timerStateStore.clear() }
        }

    @Test
    fun `restore keeps persisted running state`() =
        runTest {
            val persisted = TimerState.Paused(session(taskId = null), accumulated = 7.minutes)
            coEvery { timerStateStore.read() } returns persisted

            controller.restore()

            assertEquals(persisted, controller.state.value)
        }

    private fun session(taskId: String?) =
        RunningSession(
            id = "session",
            taskId = taskId,
            kind = FocusKind.FOCUS,
            plannedDuration = 25.minutes,
            startedAt = clock.now(),
        )

    private class FakeClock : TimeProvider {
        private var now = Instant.fromEpochMilliseconds(1_790_000_000_000)

        fun advance(duration: Duration) {
            now += duration
        }

        override fun now(): Instant = now

        override fun timeZone(): TimeZone = TimeZone.UTC
    }
}