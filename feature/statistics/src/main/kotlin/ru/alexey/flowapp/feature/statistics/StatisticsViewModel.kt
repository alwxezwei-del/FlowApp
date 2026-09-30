package ru.alexey.flowapp.feature.statistics

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.usecase.ObserveStatisticsUseCase
import ru.alexey.flowapp.core.model.DailyFocus
import ru.alexey.flowapp.core.model.StatisticsPeriod
import ru.alexey.flowapp.core.model.StatisticsSummary
import ru.alexey.flowapp.core.ui.BaseComposeViewModel
import ru.alexey.flowapp.core.ui.format.dayInitial
import ru.alexey.flowapp.core.ui.format.formatShort
import ru.alexey.flowapp.core.ui.format.formatShortDate
import kotlin.time.Duration.Companion.ZERO

@KoinViewModel
internal class StatisticsViewModel(
    private val observeStatistics: ObserveStatisticsUseCase,
    private val timeProvider: TimeProvider,
) : BaseComposeViewModel<StatisticsUiState, StatisticsUiAction>(initialState = StatisticsUiState()) {
    private val period = MutableStateFlow(StatisticsPeriod.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<StatisticsUiState> =
        period
            .flatMapLatest { selected -> observeStatistics(selected).map { it.toUiState() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = StatisticsUiState(),
            )

    override fun onAction(action: StatisticsUiAction) {
        when (action) {
            is StatisticsUiAction.SelectPeriod -> period.value = action.period
        }
    }

    private fun StatisticsSummary.toUiState(): StatisticsUiState {
        val today = timeProvider.today()
        return StatisticsUiState(
            isLoading = false,
            period = period,
            totalFocus = totalFocus.formatShort(),
            focusTrend = focusTrend,
            hasData = totalFocus > ZERO || totalTasks > 0 || habits.isNotEmpty(),
            bars = dailyFocus.toBars(period, today),
            completionRate = completionRate,
            completedTasks = completedTasks,
            totalTasks = totalTasks,
            categories = categories.map { category ->
                DistributionRowUi(
                    id = category.categoryId ?: NO_CATEGORY_ID,
                    title = category.categoryName,
                    value = category.duration.formatShort(),
                    share = category.share,
                    color = category.color,
                )
            },
            habits = habits.map { habit ->
                DistributionRowUi(
                    id = habit.habitId,
                    title = habit.habitName,
                    value = "${habit.completedDays}/${habit.scheduledDays}",
                    share = habit.rate,
                    color = habit.color,
                    icon = habit.icon,
                )
            },
        )
    }

    /**
     * Year is grouped by month (365 bars are unreadable). Month labels every fifth day to avoid
     * overlap.
     */
    private fun List<DailyFocus>.toBars(
        period: StatisticsPeriod,
        today: LocalDate,
    ): List<FocusBarUi> =
        when (period) {
            StatisticsPeriod.YEAR -> {
                groupBy { it.date.month }
                    .map { (month, days) ->
                        val total = days.fold(ZERO) { acc, daily -> acc + daily.duration }
                        FocusBarUi(
                            label = month.name.take(1),
                            minutes = total.inWholeSeconds / SECONDS_IN_MINUTE,
                            description = "${month.name.lowercase().replaceFirstChar(Char::uppercase)}, ${total.formatShort()}",
                            highlighted = month == today.month,
                        )
                    }
            }

            StatisticsPeriod.WEEK, StatisticsPeriod.MONTH -> {
                val labelStep = if (size <= DAYS_IN_WEEK) 1 else LABEL_STEP_MONTH
                mapIndexed { index, daily ->
                    FocusBarUi(
                        label = if (index % labelStep == 0) daily.date.dayInitial() else "",
                        // Fractional minutes, otherwise short sessions vanish from the chart
                        minutes = daily.duration.inWholeSeconds / SECONDS_IN_MINUTE,
                        description = "${daily.date.formatShortDate()}, ${daily.duration.formatShort()}",
                        highlighted = daily.date == today,
                    )
                }
            }
        }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val NO_CATEGORY_ID = "none"
        const val DAYS_IN_WEEK = 7
        const val LABEL_STEP_MONTH = 5
        const val SECONDS_IN_MINUTE = 60f
    }
}