package ru.alexey.flowapp.core.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitCompletion

/**
 * Habit streaks and consistency.
 *
 * @param current current streak of completed scheduled days
 * @param longest longest streak ever
 */
data class HabitStreaks(
    val current: Int = 0,
    val longest: Int = 0,
    val completedDays: Int = 0,
    val scheduledDays: Int = 0,
) {
    val completionRate: Float
        get() = if (scheduledDays <= 0) 0f else completedDays.toFloat() / scheduledDays
}

/**
 * Calculates habit streaks from completions
 *
 * Rules:
 * - only scheduled days count; missing an unscheduled day doesn't break the streak;
 * - a day counts if completions >= daily target;
 * - an unfinished today doesn't break the streak
 */
@Single
class HabitStreakCalculator(
    private val timeProvider: TimeProvider,
) {
    /**
     * @param completions any order
     * @param today reference day, defaults to today
     */
    fun calculate(
        habit: Habit,
        completions: List<HabitCompletion>,
        today: LocalDate = timeProvider.today(),
    ): HabitStreaks {
        val doneDays = completions
            .groupingBy { it.date }
            .eachCount()
            .filterValues { it >= habit.targetPerDay }
            .keys

        val createdDate = habit.createdAt.toLocalDateTime(timeProvider.timeZone()).date
        val firstDay = minOf(createdDate, completions.minOfOrNull { it.date } ?: createdDate)
        if (firstDay > today) return HabitStreaks()

        var longest = 0
        var running = 0
        var scheduledDays = 0
        var completedDays = 0

        var day = firstDay
        while (day <= today) {
            if (habit.schedule.isScheduledOn(day)) {
                scheduledDays++
                if (day in doneDays) {
                    completedDays++
                    running++
                    longest = maxOf(longest, running)
                } else {
                    running = 0
                }
            }
            day = day.plus(DatePeriod(days = 1))
        }

        return HabitStreaks(
            current = currentStreak(habit, doneDays, today),
            longest = longest,
            completedDays = completedDays,
            scheduledDays = scheduledDays,
        )
    }

    /**
     * Current streak
     */
    private fun currentStreak(
        habit: Habit,
        doneDays: Set<LocalDate>,
        today: LocalDate,
    ): Int {
        var day = today
        if (habit.schedule.isScheduledOn(day) && day !in doneDays) {
            day = day.minus(DatePeriod(days = 1))
        }

        var streak = 0
        var checkedDays = 0
        while (checkedDays < 3650) {
            if (habit.schedule.isScheduledOn(day)) {
                if (day !in doneDays) break
                streak++
            }
            day = day.minus(DatePeriod(days = 1))
            checkedDays++
        }
        return streak
    }
}