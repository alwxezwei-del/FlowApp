package ru.alexey.flowapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.FocusSession
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Completed focus session. `category_id` is copied so past stats dont change if the task
 * category does
 */
@Entity(
    tableName = "focus_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("task_id"),
        Index("ended_at"),
        Index("kind"),
    ],
)
data class FocusSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "task_id")
    val taskId: String?,
    @ColumnInfo(name = "category_id")
    val categoryId: String?,
    @ColumnInfo(name = "kind")
    val kind: String,
    @ColumnInfo(name = "planned_seconds")
    val plannedSeconds: Long,
    @ColumnInfo(name = "actual_seconds")
    val actualSeconds: Long,
    @ColumnInfo(name = "started_at")
    val startedAt: Long,
    @ColumnInfo(name = "ended_at")
    val endedAt: Long,
    @ColumnInfo(name = "completed")
    val completed: Boolean,
)

fun FocusSessionEntity.toDomain(): FocusSession =
    FocusSession(
        id = id,
        taskId = taskId,
        categoryId = categoryId,
        kind = FocusKind.parse(kind),
        plannedDuration = plannedSeconds.seconds,
        actualDuration = actualSeconds.seconds,
        startedAt = Instant.fromEpochMilliseconds(startedAt),
        endedAt = Instant.fromEpochMilliseconds(endedAt),
        completed = completed,
    )

fun FocusSession.toEntity(): FocusSessionEntity =
    FocusSessionEntity(
        id = id,
        taskId = taskId,
        categoryId = categoryId,
        kind = kind.key,
        plannedSeconds = plannedDuration.inWholeSeconds,
        actualSeconds = actualDuration.inWholeSeconds,
        startedAt = startedAt.toEpochMilliseconds(),
        endedAt = endedAt.toEpochMilliseconds(),
        completed = completed,
    )