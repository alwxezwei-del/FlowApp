package ru.alexey.flowapp.feature.home

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiState

/** Header greeting based on time of day */
enum class Greeting {
    MORNING,
    AFTERNOON,
    EVENING,
}

/**
 * Day task item, ready to render
 */
@Immutable
data class TaskUi(
    val id: String,
    val title: String,
    val subtitle: String?,
    val completed: Boolean,
    val accent: AccentColor,
    val progress: Float = 0f,
)

/**
 * Day habit item
 *
 * @param streakDays current streak, 0 = none
 */
@Immutable
data class HabitUi(
    val id: String,
    val name: String,
    val icon: String,
    val accent: AccentColor,
    val completedCount: Int,
    val target: Int,
    val streakDays: Int,
    val completed: Boolean,
)

/** Home screen state */
sealed interface HomeUiState : UiState {
    /** Initial load */
    data object Loading : HomeUiState

    /**
     * Selected day data
     */
    data class Content(
        val date: LocalDate,
        val today: LocalDate,
        val weekDays: List<LocalDate>,
        val greeting: Greeting,
        val focusTotal: String,
        val focusTrendPercent: Float?,
        val tasks: List<TaskUi>,
        val completedTasks: Int,
        val totalTasks: Int,
        val habits: List<HabitUi>,
    ) : HomeUiState {
        /** Today is selected */
        val isToday: Boolean get() = date == today
    }
}

/** Home screen actions */
sealed interface HomeUiAction : UiAction {
    data class SelectDate(
        val date: LocalDate,
    ) : HomeUiAction

    data class ToggleTask(
        val taskId: String,
        val completed: Boolean,
    ) : HomeUiAction

    data class ToggleHabit(
        val habitId: String,
        val completed: Boolean,
    ) : HomeUiAction
}