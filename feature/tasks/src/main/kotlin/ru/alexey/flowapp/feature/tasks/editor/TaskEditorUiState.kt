package ru.alexey.flowapp.feature.tasks.editor

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.Priority
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiEvent
import ru.alexey.flowapp.core.ui.UiState

@Immutable
data class CategoryOptionUi(
    val id: String,
    val name: String,
    val color: AccentColor,
)

/**
 * Task editor state
 */
data class TaskEditorUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val title: String = "",
    val description: String = "",
    val categoryId: String? = null,
    val priority: Priority = Priority.Default,
    val estimateMinutes: Int? = null,
    val dueDate: LocalDate? = null,
    val categories: List<CategoryOptionUi> = emptyList(),
) : UiState {
    val canSave: Boolean get() = title.isNotBlank()
}

/** Task editor actions. */
sealed interface TaskEditorUiAction : UiAction {
    data class TitleChanged(
        val value: String,
    ) : TaskEditorUiAction

    data class DescriptionChanged(
        val value: String,
    ) : TaskEditorUiAction

    data class CategorySelected(
        val categoryId: String?,
    ) : TaskEditorUiAction

    data class PrioritySelected(
        val priority: Priority,
    ) : TaskEditorUiAction

    data class EstimateChanged(
        val minutes: Int?,
    ) : TaskEditorUiAction

    data class DueDateChanged(
        val date: LocalDate?,
    ) : TaskEditorUiAction

    data object Save : TaskEditorUiAction

    data object Delete : TaskEditorUiAction
}

/** Task editor events */
sealed interface TaskEditorUiEvent : UiEvent {
    data object Close : TaskEditorUiEvent
}