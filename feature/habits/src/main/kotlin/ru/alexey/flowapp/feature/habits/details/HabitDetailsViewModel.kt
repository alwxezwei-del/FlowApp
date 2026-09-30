package ru.alexey.flowapp.feature.habits.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.HabitRepository
import ru.alexey.flowapp.core.domain.usecase.HabitStreakCalculator
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitCompletion
import ru.alexey.flowapp.core.model.HabitSchedule
import ru.alexey.flowapp.core.ui.BaseComposeViewModel

@KoinViewModel
internal class HabitDetailsViewModel(
    @InjectedParam private val habitId: String,
    private val habitRepository: HabitRepository,
    private val streakCalculator: HabitStreakCalculator,
    private val timeProvider: TimeProvider,
) : BaseComposeViewModel<HabitDetailsUiState, HabitDetailsUiAction>(initialState = HabitDetailsUiState()) {
    override val state: StateFlow<HabitDetailsUiState> =
        combine(
            habitRepository.observeHabit(habitId),
            habitRepository.observeCompletions(habitId),
        ) { habit, completions ->
            habit?.toState(completions) ?: HabitDetailsUiState(isLoading = false)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HabitDetailsUiState(),
        )

    override fun onAction(action: HabitDetailsUiAction) {
        when (action) {
            is HabitDetailsUiAction.ToggleDay -> toggleDay(action)
        }
    }

    private fun toggleDay(action: HabitDetailsUiAction.ToggleDay) {
        viewModelScope.launch {
            if (action.completed) {
                habitRepository.complete(habitId, action.date)
            } else {
                habitRepository.undoComplete(habitId, action.date)
            }
        }
    }

    private fun Habit.toState(completions: List<HabitCompletion>): HabitDetailsUiState {
        val today = timeProvider.today()
        val streaks = streakCalculator.calculate(this, completions, today)
        val doneDays = completions
            .groupingBy { it.date }
            .eachCount()
            .filterValues { it >= targetPerDay }
            .keys

        val firstDay = today.minus(DatePeriod(days = GRID_DAYS - 1))
        val days = List(GRID_DAYS) { offset ->
            val date = firstDay.plus(DatePeriod(days = offset))
            HabitDayUi(
                date = date,
                scheduled = schedule.isScheduledOn(date),
                completed = date in doneDays,
            )
        }

        return HabitDetailsUiState(
            isLoading = false,
            name = name,
            icon = icon,
            accent = color,
            scheduleLabel = schedule.label(),
            currentStreak = streaks.current,
            longestStreak = streaks.longest,
            completionRate = streaks.completionRate,
            days = days,
        )
    }

    private fun HabitSchedule.label(): String =
        when (this) {
            HabitSchedule.Daily -> {
                "Every day"
            }

            is HabitSchedule.SelectedDays -> {
                DayOfWeek.entries
                    .filter { it in days }
                    .joinToString(", ") {
                        it.name
                            .take(SHORT_DAY_LENGTH)
                            .lowercase()
                            .replaceFirstChar(Char::uppercase)
                    }
            }
        }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val GRID_DAYS = 30
        const val SHORT_DAY_LENGTH = 3
    }
}