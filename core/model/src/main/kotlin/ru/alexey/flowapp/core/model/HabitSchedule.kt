package ru.alexey.flowapp.core.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** Habit schedule */
sealed interface HabitSchedule {
    /** Every day */
    data object Daily : HabitSchedule

    /** Only on selected weekdays */
    data class SelectedDays(
        val days: Set<DayOfWeek>,
    ) : HabitSchedule

    fun isScheduledOn(date: LocalDate): Boolean =
        when (this) {
            Daily -> true
            is SelectedDays -> date.dayOfWeek in days
        }
}