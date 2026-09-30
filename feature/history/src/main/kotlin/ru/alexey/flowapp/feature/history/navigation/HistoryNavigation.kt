package ru.alexey.flowapp.feature.history.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import ru.alexey.flowapp.core.ui.navigation.FlowRoute
import ru.alexey.flowapp.feature.history.HistoryScreen
import ru.alexey.flowapp.feature.history.HistoryViewModel

/** Registers the history screen. */
fun NavGraphBuilder.historyScreen(onBack: () -> Unit) {
    composable<FlowRoute.History> {
        val viewModel: HistoryViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        HistoryScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
    }
}