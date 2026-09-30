package ru.alexey.flowapp.feature.tasks.list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.CategoryRepository
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.Category
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import ru.alexey.flowapp.core.ui.BaseComposeViewModel
import ru.alexey.flowapp.core.ui.DefaultUiEvent
import ru.alexey.flowapp.core.ui.format.formatFocusProgress
import ru.alexey.flowapp.core.ui.format.formatShortDate

@KoinViewModel
internal class TasksViewModel(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val timeProvider: TimeProvider,
) : BaseComposeViewModel<TasksUiState, TasksUiAction>(initialState = TasksUiState()) {
    private val filter = MutableStateFlow(TasksFilter.ALL)
    private val sort = MutableStateFlow(TasksSort.DUE_DATE)

    override val state: StateFlow<TasksUiState> =
        combine(
            taskRepository.observeTasks(),
            categoryRepository.observeCategories(),
            filter,
            sort,
        ) { tasks, categories, filter, sort ->
            val categoriesById = categories.associateBy(Category::id)
            TasksUiState(
                isLoading = false,
                tasks = tasks
                    .filter { it.matches(filter) }
                    .sortedWith(sort.comparator())
                    .map { it.toUi(categoriesById) },
                filter = filter,
                sort = sort,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = TasksUiState(),
        )

    override fun onAction(action: TasksUiAction) {
        when (action) {
            is TasksUiAction.SelectFilter -> filter.value = action.filter
            is TasksUiAction.SelectSort -> sort.value = action.sort
            is TasksUiAction.ToggleTask -> toggleTask(action.taskId, action.completed)
            is TasksUiAction.MoveToTomorrow -> moveToTomorrow(action.taskId)
            is TasksUiAction.DeleteTask -> deleteTask(action.taskId)
        }
    }

    private fun toggleTask(
        taskId: String,
        completed: Boolean,
    ) {
        viewModelScope.launch {
            taskRepository.setStatus(taskId, if (completed) TaskStatus.COMPLETED else TaskStatus.ACTIVE)
        }
    }

    private fun moveToTomorrow(taskId: String) {
        viewModelScope.launch {
            taskRepository.reschedule(taskId, timeProvider.today().plus(DatePeriod(days = 1)))
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }

    private fun Task.matches(filter: TasksFilter): Boolean {
        val today = timeProvider.today()
        val due = dueDate
        return when (filter) {
            TasksFilter.ALL -> status != TaskStatus.ARCHIVED
            TasksFilter.TODAY -> status == TaskStatus.ACTIVE && due == today
            TasksFilter.UPCOMING -> status == TaskStatus.ACTIVE && due != null && due > today
            TasksFilter.COMPLETED -> status == TaskStatus.COMPLETED
        }
    }

    /** Completed tasks always go last, then the selected order applies. */
    private fun TasksSort.comparator(): Comparator<Task> {
        val byStatus = compareBy<Task> { it.isCompleted }
        return when (this) {
            // Tasks without a due date go last
            TasksSort.DUE_DATE -> {
                byStatus
                    .thenBy { it.dueDate == null }
                    .thenBy { it.dueDate ?: LocalDate.fromEpochDays(0) }
            }

            TasksSort.PRIORITY -> {
                byStatus.thenByDescending { it.priority.weight }
            }

            TasksSort.CREATED -> {
                byStatus.thenByDescending { it.createdAt }
            }
        }
    }

    private fun Task.toUi(categoriesById: Map<String, Category>): TaskListItemUi {
        val category = categoryId?.let(categoriesById::get)
        val parts = listOfNotNull(
            category?.name,
            dueDate?.formatShortDate(),
            focusedTime.formatFocusProgress(estimate),
        )
        return TaskListItemUi(
            id = id,
            title = title,
            subtitle = parts.takeIf { it.isNotEmpty() }?.joinToString(SEPARATOR),
            completed = isCompleted,
            priority = priority,
            accent = category?.color ?: AccentColor.Default,
            hasDueDate = dueDate != null,
            progress = focusProgress,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val SEPARATOR = " · "
    }
}