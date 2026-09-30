package ru.alexey.flowapp.feature.statistics

import androidx.compose.runtime.Immutable
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.StatisticsPeriod
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiState

/**
 * Focus chart bar
 */
@Immutable
data class FocusBarUi(
    val label: String,
    val minutes: Float,
    val description: String,
    val highlighted: Boolean,
)

/**
 * Breakdown row with a progress bar
 */
@Immutable
data class DistributionRowUi(
    val id: String,
    val title: String,
    val value: String,
    val share: Float,
    val color: AccentColor,
    val icon: String? = null,
)

/**
 * Statistics screen state.
 *
 * @param focusTrend change vs previous period, null if nothing to compare
 * @param hasData the period has any data
 */
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val period: StatisticsPeriod = StatisticsPeriod.Default,
    val totalFocus: String = "",
    val focusTrend: Float? = null,
    val hasData: Boolean = false,
    val bars: List<FocusBarUi> = emptyList(),
    val completionRate: Float = 0f,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val categories: List<DistributionRowUi> = emptyList(),
    val habits: List<DistributionRowUi> = emptyList(),
) : UiState {
    val isEmpty: Boolean get() = !hasData
}

/** Statistics actions */
sealed interface StatisticsUiAction : UiAction {
    data class SelectPeriod(
        val period: StatisticsPeriod,
    ) : StatisticsUiAction
}