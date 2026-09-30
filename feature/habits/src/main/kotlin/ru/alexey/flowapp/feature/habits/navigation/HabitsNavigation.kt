package ru.alexey.flowapp.feature.habits.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alexey.flowapp.core.ui.CollectUiEvents
import ru.alexey.flowapp.core.ui.navigation.FlowRoute
import ru.alexey.flowapp.feature.habits.details.HabitDetailsScreen
import ru.alexey.flowapp.feature.habits.details.HabitDetailsViewModel
import ru.alexey.flowapp.feature.habits.editor.HabitEditorScreen
import ru.alexey.flowapp.feature.habits.editor.HabitEditorUiEvent
import ru.alexey.flowapp.feature.habits.editor.HabitEditorViewModel
import ru.alexey.flowapp.feature.habits.list.HabitsScreen
import ru.alexey.flowapp.feature.habits.list.HabitsViewModel

/** Registers habit screens. */
fun NavGraphBuilder.habitsScreen(
    onOpenHabit: (String) -> Unit,
    onCreateHabit: () -> Unit,
    onBack: () -> Unit,
) {
    composable<FlowRoute.Habits> {
        val viewModel: HabitsViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        HabitsScreen(
            state = state,
            onAction = viewModel::onAction,
            onOpenHabit = onOpenHabit,
            onCreateHabit = onCreateHabit,
            onBack = onBack,
        )
    }
}

fun NavGraphBuilder.habitDetailsScreen(
    onEditHabit: (String) -> Unit,
    onBack: () -> Unit,
) {
    composable<FlowRoute.HabitDetails> { entry ->
        val route = entry.toRoute<FlowRoute.HabitDetails>()
        val viewModel: HabitDetailsViewModel = koinViewModel { parametersOf(route.habitId) }
        val state by viewModel.state.collectAsStateWithLifecycle()

        HabitDetailsScreen(
            state = state,
            onAction = viewModel::onAction,
            onEdit = { onEditHabit(route.habitId) },
            onBack = onBack,
        )
    }
}

fun NavGraphBuilder.habitEditorScreen(onBack: () -> Unit) {
    composable<FlowRoute.HabitEditor> { entry ->
        val route = entry.toRoute<FlowRoute.HabitEditor>()
        val viewModel: HabitEditorViewModel = koinViewModel { parametersOf(route.habitId) }
        val state by viewModel.state.collectAsStateWithLifecycle()

        CollectUiEvents<HabitEditorUiEvent>(viewModel.events, onNavigateUp = onBack) { event ->
            when (event) {
                HabitEditorUiEvent.Close -> onBack()
            }
        }

        HabitEditorScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
    }
}