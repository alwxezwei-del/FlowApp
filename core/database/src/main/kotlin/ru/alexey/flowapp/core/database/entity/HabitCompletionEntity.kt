package ru.alexey.flowapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.model.HabitCompletion
import kotlin.time.Instant

/** Habit completion */
@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("habit_id"),
        Index("date"),
        Index(value = ["habit_id", "date"]),
    ],
)
data class HabitCompletionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "habit_id")
    val habitId: String,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long,
)

fun HabitCompletionEntity.toDomain(): HabitCompletion =
    HabitCompletion(
        id = id,
        habitId = habitId,
        date = LocalDate.fromEpochDays(date.toInt()),
        completedAt = Instant.fromEpochMilliseconds(completedAt),
    )

fun HabitCompletion.toEntity(): HabitCompletionEntity =
    HabitCompletionEntity(
        id = id,
        habitId = habitId,
        date = date.toEpochDays().toLong(),
        completedAt = completedAt.toEpochMilliseconds(),
    )