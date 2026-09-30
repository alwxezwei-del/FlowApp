package ru.alexey.flowapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitCompletion
import ru.alexey.flowapp.core.model.HabitDayProgress

/** Habits and their completion history */
interface HabitRepository {
    /** Active habits */
    fun observeHabits(): Flow<List<Habit>>

    /** All habits, including archived */
    fun observeAllHabits(): Flow<List<Habit>>

    fun observeHabit(id: String): Flow<Habit?>

    /** Habits scheduled for the day, with that day progress */
    fun observeProgressForDate(date: LocalDate): Flow<List<HabitDayProgress>>

    /** Completions in range */
    fun observeCompletions(range: DateRange): Flow<List<HabitCompletion>>

    /** All completions of one habit */
    fun observeCompletions(habitId: String): Flow<List<HabitCompletion>>

    /** All completions of all habits, used for streaks */
    fun observeAllCompletions(): Flow<List<HabitCompletion>>

    suspend fun createHabit(habit: Habit)

    suspend fun updateHabit(habit: Habit)

    /** Adds a completion for the given day */
    suspend fun complete(
        habitId: String,
        date: LocalDate,
    )

    /** Removes the latest completion for the day */
    suspend fun undoComplete(
        habitId: String,
        date: LocalDate,
    )

    /** Archives or unarchives a habit */
    suspend fun setArchived(
        id: String,
        archived: Boolean,
    )

    suspend fun deleteHabit(id: String)
}