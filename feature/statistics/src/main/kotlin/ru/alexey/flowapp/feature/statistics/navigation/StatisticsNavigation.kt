package ru.alexey.flowapp.feature.statistics.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import ru.alexey.flowapp.core.ui.navigation.FlowRoute
import ru.alexey.flowapp.feature.statistics.StatisticsScreen
import ru.alexey.flowapp.feature.statistics.StatisticsViewModel

fun NavGraphBuilder.statisticsScreen() {
    composable<FlowRoute.Statistics> {
        val viewModel: StatisticsViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        StatisticsScreen(state = state, onAction = viewModel::onAction)
    }
}