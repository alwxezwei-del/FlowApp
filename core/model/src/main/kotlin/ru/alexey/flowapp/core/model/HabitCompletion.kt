package ru.alexey.flowapp.core.model

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * Single habit completion
 *
 * @param date day the completion counts for
 * @param completedAt when the user marked it
 */
data class HabitCompletion(
    val id: String = newId(),
    val habitId: String,
    val date: LocalDate,
    val completedAt: Instant,
)

/** Habit progress for a specific day */
data class HabitDayProgress(
    val habit: Habit,
    val date: LocalDate,
    val completedCount: Int,
    val scheduled: Boolean,
) {
    /** Daily target reached */
    val isDone: Boolean get() = completedCount >= habit.targetPerDay

    /** Daily target progress */
    val progress: Float
        get() = if (habit.targetPerDay <= 0) 0f else (completedCount.toFloat() / habit.targetPerDay).coerceIn(0f, 1f)
}