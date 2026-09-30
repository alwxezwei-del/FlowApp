package ru.alexey.flowapp.feature.tasks.editor

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.CategoryRepository
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.ui.BaseComposeViewModel
import kotlin.time.Duration.Companion.minutes

@KoinViewModel
internal class TaskEditorViewModel(
    @InjectedParam private val taskId: String?,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val timeProvider: TimeProvider,
) : BaseComposeViewModel<TaskEditorUiState, TaskEditorUiAction>(
        initialState = TaskEditorUiState(isEditing = taskId != null),
    ) {
    init {
        load()
    }

    override fun onAction(action: TaskEditorUiAction) {
        when (action) {
            is TaskEditorUiAction.TitleChanged -> _state.update { it.copy(title = action.value) }
            is TaskEditorUiAction.DescriptionChanged -> _state.update { it.copy(description = action.value) }
            is TaskEditorUiAction.CategorySelected -> _state.update { it.copy(categoryId = action.categoryId) }
            is TaskEditorUiAction.PrioritySelected -> _state.update { it.copy(priority = action.priority) }
            is TaskEditorUiAction.EstimateChanged -> _state.update { it.copy(estimateMinutes = action.minutes) }
            is TaskEditorUiAction.DueDateChanged -> _state.update { it.copy(dueDate = action.date) }
            TaskEditorUiAction.Save -> save()
            TaskEditorUiAction.Delete -> delete()
        }
    }

    private fun load() {
        viewModelScope.launch {
            val categories = categoryRepository
                .observeCategories()
                .first()
                .map { CategoryOptionUi(id = it.id, name = it.name, color = it.color) }
            val task = taskId?.let { taskRepository.getTask(it) }

            _state.update { current ->
                current.copy(
                    isLoading = false,
                    categories = categories,
                    title = task?.title ?: current.title,
                    description = task?.description.orEmpty(),
                    categoryId = task?.categoryId,
                    priority = task?.priority ?: current.priority,
                    estimateMinutes = task?.estimate?.inWholeMinutes?.toInt(),
                    // New tasks default to today so they show up on Today right away
                    dueDate = task?.dueDate ?: timeProvider.today().takeIf { taskId == null },
                )
            }
        }
    }

    private fun save() {
        val current = state.value
        if (!current.canSave) return

        viewModelScope.launch {
            val existing = taskId?.let { taskRepository.getTask(it) }
            val task = existing?.copy(
                title = current.title.trim(),
                description = current.description.trim().takeIf { it.isNotEmpty() },
                categoryId = current.categoryId,
                estimate = current.estimateMinutes?.minutes,
                dueDate = current.dueDate,
                priority = current.priority,
            ) ?: Task(
                title = current.title.trim(),
                description = current.description.trim().takeIf { it.isNotEmpty() },
                categoryId = current.categoryId,
                estimate = current.estimateMinutes?.minutes,
                dueDate = current.dueDate,
                priority = current.priority,
                createdAt = timeProvider.now(),
            )

            if (existing == null) taskRepository.createTask(task) else taskRepository.updateTask(task)
            sendEvent(TaskEditorUiEvent.Close)
        }
    }

    private fun delete() {
        val id = taskId ?: return
        viewModelScope.launch {
            taskRepository.deleteTask(id)
            sendEvent(TaskEditorUiEvent.Close)
        }
    }
}