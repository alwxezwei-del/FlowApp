package ru.alexey.flowapp.core.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitCompletion
import ru.alexey.flowapp.core.model.HabitSchedule
import kotlin.time.Instant

/**
 * Streak calculation tests: unfinished today, unscheduled days, partial daily target, empty
 * schedule.
 */
class HabitStreakCalculatorTest {
    private val today = LocalDate(2026, 9, 30)
    private val calculator = HabitStreakCalculator(FixedTimeProvider(today))

    @Test
    fun `no completions means no streak`() {
        val habit = dailyHabit(createdAt = today.minusDays(10))

        val streaks = calculator.calculate(habit, completions = emptyList(), today = today)

        assertEquals(0, streaks.current)
        assertEquals(0, streaks.longest)
    }

    @Test
    fun `consecutive days build the current streak`() {
        val habit = dailyHabit(createdAt = today.minusDays(5))
        val completions = completionsFor(habit, today, today.minusDays(1), today.minusDays(2))

        val streaks = calculator.calculate(habit, completions, today)

        assertEquals(3, streaks.current)
        assertEquals(3, streaks.longest)
    }

    @Test
    fun `an unfinished today does not break the streak`() {
        // Morning: today isn't done yet, but the two previous days are, so the streak holds
        val habit = dailyHabit(createdAt = today.minusDays(5))
        val completions = completionsFor(habit, today.minusDays(1), today.minusDays(2))

        val streaks = calculator.calculate(habit, completions, today)

        assertEquals(2, streaks.current)
    }

    @Test
    fun `a missed day resets the current streak but keeps the longest one`() {
        val habit = dailyHabit(createdAt = today.minusDays(10))
        val completions = completionsFor(
            habit,
            today.minusDays(9),
            today.minusDays(8),
            today.minusDays(7),
            today.minusDays(6),
            // Missed five days ago
            today,
        )

        val streaks = calculator.calculate(habit, completions, today)

        assertEquals(1, streaks.current)
        assertEquals(4, streaks.longest)
    }

    @Test
    fun `days outside the schedule do not break the streak`() {
        // Mon, Wed, Fri: weekends are skipped by schedule, not by the user
        val habit = Habit(
            id = "habit",
            name = "Workout",
            icon = "workout",
            schedule = HabitSchedule.SelectedDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
            createdAt = today.minusDays(14).toInstant(),
        )
        // 30.09.2026 is Wednesday; previous scheduled days are Mon 28th and Fri 25th
        val completions = completionsFor(habit, today, today.minusDays(2), today.minusDays(5))

        val streaks = calculator.calculate(habit, completions, today)

        assertEquals(3, streaks.current)
    }

    @Test
    fun `a day counts only when the daily target is reached`() {
        val habit = dailyHabit(createdAt = today.minusDays(3), targetPerDay = 2)
        val completions = completionsFor(habit, today.minusDays(1)) +
            completionsFor(habit, today.minusDays(1)) +
            // Today's target is half done, so the day doesn't count
            completionsFor(habit, today)

        val streaks = calculator.calculate(habit, completions, today)

        assertEquals(1, streaks.current)
    }

    @Test
    fun `completion rate counts only scheduled days`() {
        val habit = dailyHabit(createdAt = today.minusDays(3))
        val completions = completionsFor(habit, today, today.minusDays(1))

        val streaks = calculator.calculate(habit, completions, today)

        // Four scheduled days: today and three previous
        assertEquals(4, streaks.scheduledDays)
        assertEquals(2, streaks.completedDays)
        assertEquals(0.5f, streaks.completionRate, TOLERANCE)
    }

    @Test
    fun `an empty schedule produces no streak instead of looping forever`() {
        val habit = Habit(
            id = "habit",
            name = "Never",
            icon = "star",
            schedule = HabitSchedule.SelectedDays(emptySet()),
            createdAt = today.minusDays(5).toInstant(),
        )

        val streaks = calculator.calculate(habit, completions = emptyList(), today = today)

        assertEquals(0, streaks.current)
        assertEquals(0, streaks.scheduledDays)
    }

    @Test
    fun `a habit created in the future has no history`() {
        val habit = dailyHabit(createdAt = today.plusDays(3))

        val streaks = calculator.calculate(habit, completions = emptyList(), today = today)

        assertEquals(0, streaks.scheduledDays)
    }

    private fun dailyHabit(
        createdAt: LocalDate,
        targetPerDay: Int = 1,
    ) = Habit(
        id = "habit",
        name = "Read",
        icon = "read",
        schedule = HabitSchedule.Daily,
        targetPerDay = targetPerDay,
        createdAt = createdAt.toInstant(),
    )

    private fun completionsFor(
        habit: Habit,
        vararg dates: LocalDate,
    ): List<HabitCompletion> =
        dates.map { date ->
            HabitCompletion(habitId = habit.id, date = date, completedAt = date.toInstant())
        }

    private fun LocalDate.minusDays(days: Int) = minus(DatePeriod(days = days))

    private fun LocalDate.plusDays(days: Int) = minus(DatePeriod(days = -days))

    private fun LocalDate.toInstant(): Instant = Instant.fromEpochMilliseconds(toEpochDays().toLong() * MILLIS_IN_DAY)

    /** @param today date seen by the calculator instead of the system one */
    private class FixedTimeProvider(
        private val today: LocalDate,
    ) : TimeProvider {
        override fun now(): Instant = Instant.fromEpochMilliseconds(today.toEpochDays().toLong() * MILLIS_IN_DAY)

        override fun timeZone(): TimeZone = TimeZone.UTC
    }

    private companion object {
        const val MILLIS_IN_DAY = 86_400_000L
        const val TOLERANCE = 0.0001f
    }
}