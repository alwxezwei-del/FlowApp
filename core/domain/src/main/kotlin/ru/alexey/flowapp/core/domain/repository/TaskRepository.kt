package ru.alexey.flowapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import kotlin.time.Duration

/** Tasks */
interface TaskRepository {
    /** All tasks */
    fun observeTasks(): Flow<List<Task>>

    fun observeTasksForDate(date: LocalDate): Flow<List<Task>>

    /** Tasks with a due date within the range */
    fun observeTasksInRange(range: DateRange): Flow<List<Task>>

    fun observeTask(id: String): Flow<Task?>

    suspend fun getTask(id: String): Task?

    suspend fun createTask(task: Task)

    suspend fun updateTask(task: Task)

    /** Sets task status */
    suspend fun setStatus(
        id: String,
        status: TaskStatus,
    )

    /**
     * Moves the task to another day
     */
    suspend fun reschedule(
        id: String,
        date: LocalDate?,
    )

    /** Adds a completed focus session's duration to the task */
    suspend fun addFocusedTime(
        id: String,
        duration: Duration,
    )

    suspend fun deleteTask(id: String)
}