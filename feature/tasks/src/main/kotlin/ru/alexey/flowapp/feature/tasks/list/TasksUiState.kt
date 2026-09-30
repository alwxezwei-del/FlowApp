package ru.alexey.flowapp.feature.tasks.list

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.Priority
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiState
import ru.alexey.flowapp.feature.tasks.R

/** Task list filter */
enum class TasksFilter(
    @param:StringRes val labelRes: Int,
) {
    ALL(R.string.tasks_filter_all),
    TODAY(R.string.tasks_filter_today),
    UPCOMING(R.string.tasks_filter_upcoming),
    COMPLETED(R.string.tasks_filter_completed),
}

/** Sort order */
enum class TasksSort(
    @param:StringRes val labelRes: Int,
) {
    DUE_DATE(R.string.tasks_sort_due_date),
    PRIORITY(R.string.tasks_sort_priority),
    CREATED(R.string.tasks_sort_created),
}

/**
 * Task list item
 *
 * @param subtitle Work Sep 30 45m
 * @param hasDueDate enables "move to tomorrow"
 */
@Immutable
data class TaskListItemUi(
    val id: String,
    val title: String,
    val subtitle: String?,
    val completed: Boolean,
    val priority: Priority,
    val accent: AccentColor,
    val hasDueDate: Boolean,
    val progress: Float = 0f,
)

/** Tasks screen state */
data class TasksUiState(
    val isLoading: Boolean = true,
    val tasks: List<TaskListItemUi> = emptyList(),
    val filter: TasksFilter = TasksFilter.ALL,
    val sort: TasksSort = TasksSort.DUE_DATE,
) : UiState

/** Tasks screen actions */
sealed interface TasksUiAction : UiAction {
    data class SelectFilter(
        val filter: TasksFilter,
    ) : TasksUiAction

    data class SelectSort(
        val sort: TasksSort,
    ) : TasksUiAction

    data class ToggleTask(
        val taskId: String,
        val completed: Boolean,
    ) : TasksUiAction

    data class MoveToTomorrow(
        val taskId: String,
    ) : TasksUiAction

    data class DeleteTask(
        val taskId: String,
    ) : TasksUiAction
}