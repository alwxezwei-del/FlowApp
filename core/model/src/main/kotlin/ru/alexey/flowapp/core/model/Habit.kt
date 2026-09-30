package ru.alexey.flowapp.core.model

import kotlin.time.Instant

/**
 * Habit with a schedule and daily target
 *
 * @param targetPerDay completions required per day, min 1
 * @param reminderMinuteOfDay minutes from midnight, null = no reminder
 * @param archived hidden from active lists, history kept
 */
data class Habit(
    val id: String = newId(),
    val name: String,
    val icon: String,
    val color: AccentColor = AccentColor.Default,
    val schedule: HabitSchedule = HabitSchedule.Daily,
    val targetPerDay: Int = 1,
    val reminderMinuteOfDay: Int? = null,
    val note: String? = null,
    val createdAt: Instant,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
)