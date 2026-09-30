package ru.alexey.flowapp.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory
import ru.alexey.flowapp.core.domain.repository.HabitRepository

/**
 * Streaks for all habits in one flow
 */
@Factory
class ObserveHabitStreaksUseCase(
    private val habitRepository: HabitRepository,
    private val streakCalculator: HabitStreakCalculator,
) {
    /** @return streaks by habit id */
    operator fun invoke(): Flow<Map<String, HabitStreaks>> =
        combine(
            habitRepository.observeAllHabits(),
            habitRepository.observeAllCompletions(),
        ) { habits, completions ->
            val byHabit = completions.groupBy { it.habitId }
            habits.associate { habit ->
                habit.id to streakCalculator.calculate(habit, byHabit[habit.id].orEmpty())
            }
        }
}