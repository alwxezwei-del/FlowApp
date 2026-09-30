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
import ru.alexey.flowapp.core.model.HistoryEvent
import ru.alexey.flowapp.core.model.HistoryFilter
import ru.alexey.flowapp.core.ui.BaseComposeViewModel
import ru.alexey.flowapp.core.ui.format.formatShort
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
                    date = date,
                    relative = when (date) {
                        today -> RelativeDay.TODAY
                        yesterday -> RelativeDay.YESTERDAY
                        else -> null
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
                icon = FlowIconKey.CODE,
                accent = AccentColor.PURPLE,
                content = HistoryEventContent.Focus(
                    duration = duration.formatShort(),
                    kind = kind,
                    taskTitle = taskTitle,
                    interrupted = !completed,
                ),
            )

            is HistoryEvent.TaskCompleted -> HistoryEventUi(
                id = id,
                time = time,
                icon = FlowIconKey.WORK,
                accent = color,
                content = HistoryEventContent.Task(title = title, categoryName = categoryName),
            )

            is HistoryEvent.HabitCompleted -> HistoryEventUi(
                id = id,
                time = time,
                icon = icon,
                accent = color,
                content = HistoryEventContent.Habit(name = habitName),
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val HISTORY_MONTHS = 3
    }
}