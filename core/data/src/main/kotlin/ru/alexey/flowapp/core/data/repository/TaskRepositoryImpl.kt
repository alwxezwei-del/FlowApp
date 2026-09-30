package ru.alexey.flowapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.database.dao.TaskDao
import ru.alexey.flowapp.core.database.entity.toDomain
import ru.alexey.flowapp.core.database.entity.toEntity
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import kotlin.time.Duration

@Single
internal class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val timeProvider: TimeProvider,
) : TaskRepository {
    override fun observeTasks(): Flow<List<Task>> = taskDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeTasksForDate(date: LocalDate): Flow<List<Task>> =
        taskDao.observeForDate(date.epochDay()).map { entities -> entities.map { it.toDomain() } }

    override fun observeTasksInRange(range: DateRange): Flow<List<Task>> =
        taskDao
            .observeInRange(range.start.epochDay(), range.endInclusive.epochDay())
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeTask(id: String): Flow<Task?> = taskDao.observeById(id).map { it?.toDomain() }

    override suspend fun getTask(id: String): Task? = taskDao.getById(id)?.toDomain()

    override suspend fun createTask(task: Task) = taskDao.insert(task.toEntity())

    override suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())

    override suspend fun setStatus(
        id: String,
        status: TaskStatus,
    ) {
        val completedAt = timeProvider.now().toEpochMilliseconds().takeIf { status == TaskStatus.COMPLETED }
        taskDao.updateStatus(id = id, status = status.key, completedAt = completedAt)
    }

    override suspend fun reschedule(
        id: String,
        date: LocalDate?,
    ) = taskDao.updateDueDate(id, date?.epochDay())

    override suspend fun addFocusedTime(
        id: String,
        duration: Duration,
    ) = taskDao.addFocusedSeconds(id, duration.inWholeSeconds)

    override suspend fun deleteTask(id: String) = taskDao.deleteById(id)
}

internal fun LocalDate.epochDay(): Long = toEpochDays().toLong()