package ru.alexey.flowapp.feature.history

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.usecase.ObserveHistoryUseCase
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.FlowIconKey
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.HistoryEvent
import ru.alexey.flowapp.core.model.HistoryFilter
import ru.alexey.flowapp.core.ui.BaseComposeViewModel
import ru.alexey.flowapp.core.ui.format.formatShort
import ru.alexey.flowapp.core.ui.format.formatShortDate
import ru.alexey.flowapp.core.ui.format.formatTime

@KoinViewModel
internal class HistoryViewModel(
    private val observeHistory: ObserveHistoryUseCase,
    private val timeProvider: TimeProvider,
) : BaseComposeViewModel<HistoryUiState, HistoryUiAction>(initialState = HistoryUiState()) {
    private val filter = MutableStateFlow(HistoryFilter.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<HistoryUiState> =
        filter
            .flatMapLatest { selected ->
                val today = timeProvider.today()
                val range = DateRange(today.minus(DatePeriod(months = HISTORY_MONTHS)), today)
                observeHistory(range, selected).map { events -> events.toUiState(selected, today) }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = HistoryUiState(),
            )

    override fun onAction(action: HistoryUiAction) {
        when (action) {
            is HistoryUiAction.SelectFilter -> filter.value = action.filter
        }
    }

    private fun List<HistoryEvent>.toUiState(
        filter: HistoryFilter,
        today: LocalDate,
    ): HistoryUiState {
        val zone = timeProvider.timeZone()
        val yesterday = today.minus(DatePeriod(days = 1))

        val days = groupBy { it.timestamp.toLocalDateTime(zone).date }
            .toSortedMap(compareByDescending { it })
            .map { (date, events) ->
                HistoryDayUi(
                    title = when (date) {
                        today -> TODAY_TITLE
                        yesterday -> YESTERDAY_TITLE
                        else -> date.formatShortDate()
                    },
                    events = events.map { it.toUi(zone) },
                )
            }

        return HistoryUiState(isLoading = false, filter = filter, days = days)
    }

    private fun HistoryEvent.toUi(zone: kotlinx.datetime.TimeZone): HistoryEventUi {
        val time = timestamp.toLocalDateTime(zone).time.formatTime()
        return when (this) {
            is HistoryEvent.FocusFinished -> HistoryEventUi(
                id = id,
                time = time,
                title = when (kind) {
                    FocusKind.FOCUS -> "${duration.formatShort()} focus"
                    FocusKind.SHORT_BREAK -> "${duration.formatShort()} short break"
                    FocusKind.LONG_BREAK -> "${duration.formatShort()} long break"
                },
                subtitle = listOfNotNull(taskTitle, INTERRUPTED_LABEL.takeIf { !completed })
                    .joinToString(SEPARATOR)
                    .takeIf { it.isNotEmpty() },
                kind = HistoryEventKind.FOCUS,
                icon = FlowIconKey.CODE,
                accent = AccentColor.PURPLE,
            )

            is HistoryEvent.TaskCompleted -> HistoryEventUi(
                id = id,
                time = time,
                title = title,
                subtitle = categoryName,
                kind = HistoryEventKind.TASK,
                icon = FlowIconKey.WORK,
                accent = color,
            )

            is HistoryEvent.HabitCompleted -> HistoryEventUi(
                id = id,
                time = time,
                title = habitName,
                subtitle = HABIT_SUBTITLE,
                kind = HistoryEventKind.HABIT,
                icon = icon,
                accent = color,
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val HISTORY_MONTHS = 3
        const val TODAY_TITLE = "Today"
        const val YESTERDAY_TITLE = "Yesterday"
        const val HABIT_SUBTITLE = "Habit completed"
        const val INTERRUPTED_LABEL = "interrupted"
        const val SEPARATOR = " · "
    }
}