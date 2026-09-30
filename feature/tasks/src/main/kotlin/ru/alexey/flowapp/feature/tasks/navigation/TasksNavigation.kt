package ru.alexey.flowapp.feature.tasks.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.ui.CollectUiEvents
import ru.alexey.flowapp.core.ui.navigation.FlowRoute
import ru.alexey.flowapp.feature.tasks.editor.TaskEditorScreen
import ru.alexey.flowapp.feature.tasks.editor.TaskEditorUiEvent
import ru.alexey.flowapp.feature.tasks.editor.TaskEditorViewModel
import ru.alexey.flowapp.feature.tasks.list.TasksScreen
import ru.alexey.flowapp.feature.tasks.list.TasksViewModel

fun NavGraphBuilder.tasksScreen(
    onOpenTask: (String) -> Unit,
    onCreateTask: () -> Unit,
    onStartFocus: (String) -> Unit,
) {
    composable<FlowRoute.Tasks> {
        val viewModel: TasksViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        TasksScreen(
            state = state,
            onAction = viewModel::onAction,
            onOpenTask = onOpenTask,
            onCreateTask = onCreateTask,
            onStartFocus = onStartFocus,
        )
    }
}

fun NavGraphBuilder.taskEditorScreen(onBack: () -> Unit) {
    composable<FlowRoute.TaskEditor> { entry ->
        val route = entry.toRoute<FlowRoute.TaskEditor>()
        val viewModel: TaskEditorViewModel = koinViewModel { parametersOf(route.taskId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val timeProvider: TimeProvider = koinInject()
        val today = remember(timeProvider) { timeProvider.today() }

        CollectUiEvents<TaskEditorUiEvent>(viewModel.events, onNavigateUp = onBack) { event ->
            when (event) {
                TaskEditorUiEvent.Close -> onBack()
            }
        }

        TaskEditorScreen(
            state = state,
            today = today,
            onAction = viewModel::onAction,
            onBack = onBack,
        )
    }
}