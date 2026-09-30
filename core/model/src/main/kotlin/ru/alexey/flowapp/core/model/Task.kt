package ru.alexey.flowapp.core.model

import kotlinx.datetime.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Instant

/**
 * User task. [focusedTime] is accumulated from linked focus sessions, not edited manually
 *
 * @param estimate planned duration, null if not set
 * @param dueDate null = no due date
 * @param completedAt not null only for [TaskStatus.COMPLETED]
 */
data class Task(
    val id: String = newId(),
    val title: String,
    val description: String? = null,
    val categoryId: String? = null,
    val estimate: Duration? = null,
    val focusedTime: Duration = ZERO,
    val dueDate: LocalDate? = null,
    val priority: Priority = Priority.Default,
    val status: TaskStatus = TaskStatus.ACTIVE,
    val createdAt: Instant,
    val completedAt: Instant? = null,
) {
    val isCompleted: Boolean get() = status == TaskStatus.COMPLETED

    val focusProgress: Float
        get() = when {
            focusedTime <= ZERO -> 0f
            estimate == null || estimate <= ZERO -> 1f
            else -> (focusedTime / estimate).toFloat().coerceIn(0f, 1f)
        }
}