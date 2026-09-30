package ru.alexey.flowapp.feature.habits.editor

import kotlinx.datetime.DayOfWeek
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiEvent
import ru.alexey.flowapp.core.ui.UiState

/**
 * Habit editor state.
 */
data class HabitEditorUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val name: String = "",
    val icon: String = ru.alexey.flowapp.core.model.FlowIconKey.Default,
    val color: AccentColor = AccentColor.Default,
    val daily: Boolean = true,
    val selectedDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val targetPerDay: Int = 1,
    val reminderMinuteOfDay: Int? = null,
    val note: String = "",
) : UiState {
    val canSave: Boolean get() = name.isNotBlank() && (daily || selectedDays.isNotEmpty())
}

/** Habit editor actions */
sealed interface HabitEditorUiAction : UiAction {
    data class NameChanged(
        val value: String,
    ) : HabitEditorUiAction

    data class IconSelected(
        val icon: String,
    ) : HabitEditorUiAction

    data class ColorSelected(
        val color: AccentColor,
    ) : HabitEditorUiAction

    data class ScheduleChanged(
        val daily: Boolean,
    ) : HabitEditorUiAction

    data class DayToggled(
        val day: DayOfWeek,
    ) : HabitEditorUiAction

    data class TargetChanged(
        val delta: Int,
    ) : HabitEditorUiAction

    data class ReminderChanged(
        val minuteOfDay: Int?,
    ) : HabitEditorUiAction

    data class NoteChanged(
        val value: String,
    ) : HabitEditorUiAction

    data object Save : HabitEditorUiAction

    data object Delete : HabitEditorUiAction
}

/** Habit editor events */
sealed interface HabitEditorUiEvent : UiEvent {
    /** Habit saved or deleted */
    data object Close : HabitEditorUiEvent
}