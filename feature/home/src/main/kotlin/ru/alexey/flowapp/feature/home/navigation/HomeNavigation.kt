package ru.alexey.flowapp.feature.home.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import ru.alexey.flowapp.core.ui.navigation.FlowRoute
import ru.alexey.flowapp.feature.home.HomeScreen
import ru.alexey.flowapp.feature.home.HomeViewModel

fun NavGraphBuilder.homeScreen(
    onOpenTask: (String) -> Unit,
    onOpenHabit: (String) -> Unit,
    onOpenHabits: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    composable<FlowRoute.Home> {
        val viewModel: HomeViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        HomeScreen(
            state = state,
            onAction = viewModel::onAction,
            onOpenTask = onOpenTask,
            onOpenHabit = onOpenHabit,
            onOpenHabits = onOpenHabits,
            onOpenHistory = onOpenHistory,
            onOpenSettings = onOpenSettings,
        )
    }
}