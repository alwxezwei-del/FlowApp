package ru.alexey.flowapp.feature.habits.list

import androidx.compose.runtime.Immutable
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiState

/**
 * Habit list item
 *
 * @param completedToday completions today
 * @param streakDays current streak, 0 = none
 */
@Immutable
data class HabitListItemUi(
    val id: String,
    val name: String,
    val icon: String,
    val accent: AccentColor,
    val completedToday: Int,
    val target: Int,
    val streakDays: Int,
    val completed: Boolean,
    val scheduledToday: Boolean,
    val archived: Boolean,
)

/**
 * Habits screen state
 *
 * @param otherHabits active habits not scheduled today
 */
data class HabitsUiState(
    val isLoading: Boolean = true,
    val todayHabits: List<HabitListItemUi> = emptyList(),
    val otherHabits: List<HabitListItemUi> = emptyList(),
    val archivedHabits: List<HabitListItemUi> = emptyList(),
) : UiState {
    val isEmpty: Boolean
        get() = todayHabits.isEmpty() && otherHabits.isEmpty() && archivedHabits.isEmpty()
}

/** Habits screen actions */
sealed interface HabitsUiAction : UiAction {
    data class ToggleHabit(
        val habitId: String,
        val completed: Boolean,
    ) : HabitsUiAction

    data class SetArchived(
        val habitId: String,
        val archived: Boolean,
    ) : HabitsUiAction

    data class DeleteHabit(
        val habitId: String,
    ) : HabitsUiAction
}