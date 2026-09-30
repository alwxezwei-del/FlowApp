package ru.alexey.flowapp.feature.habits.details

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiState

@Immutable
data class HabitDayUi(
    val date: LocalDate,
    val scheduled: Boolean,
    val completed: Boolean,
)

/**
 * Habit details state
 */
data class HabitDetailsUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val icon: String = "",
    val accent: AccentColor = AccentColor.Default,
    val scheduleLabel: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionRate: Float = 0f,
    val days: List<HabitDayUi> = emptyList(),
) : UiState

/** Habit details actions */
sealed interface HabitDetailsUiAction : UiAction {
    data class ToggleDay(
        val date: LocalDate,
        val completed: Boolean,
    ) : HabitDetailsUiAction
}