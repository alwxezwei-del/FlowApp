package ru.alexey.flowapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.model.Priority
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** Task row */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("category_id"),
        Index("due_date"),
        Index("status"),
    ],
)
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "category_id")
    val categoryId: String?,
    @ColumnInfo(name = "estimate_seconds")
    val estimateSeconds: Long?,
    @ColumnInfo(name = "focused_seconds")
    val focusedSeconds: Long,
    @ColumnInfo(name = "due_date")
    val dueDate: Long?,
    @ColumnInfo(name = "priority")
    val priority: String,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,
)

fun TaskEntity.toDomain(): Task =
    Task(
        id = id,
        title = title,
        description = description,
        categoryId = categoryId,
        estimate = estimateSeconds?.seconds,
        focusedTime = focusedSeconds.seconds,
        dueDate = dueDate?.let { LocalDate.fromEpochDays(it.toInt()) },
        priority = Priority.parse(priority),
        status = TaskStatus.parse(status),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        completedAt = completedAt?.let(Instant::fromEpochMilliseconds),
    )

fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        title = title,
        description = description,
        categoryId = categoryId,
        estimateSeconds = estimate?.inWholeSeconds,
        focusedSeconds = focusedTime.inWholeSeconds,
        dueDate = dueDate?.toEpochDays()?.toLong(),
        priority = priority.key,
        status = status.key,
        createdAt = createdAt.toEpochMilliseconds(),
        completedAt = completedAt?.toEpochMilliseconds(),
    )