package ru.alexey.flowapp.feature.habits.list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.HabitRepository
import ru.alexey.flowapp.core.domain.usecase.HabitStreaks
import ru.alexey.flowapp.core.domain.usecase.ObserveHabitStreaksUseCase
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitCompletion
import ru.alexey.flowapp.core.ui.BaseComposeViewModel

@KoinViewModel
internal class HabitsViewModel(
    private val habitRepository: HabitRepository,
    private val observeHabitStreaks: ObserveHabitStreaksUseCase,
    private val timeProvider: TimeProvider,
) : BaseComposeViewModel<HabitsUiState, HabitsUiAction>(initialState = HabitsUiState()) {
    override val state: StateFlow<HabitsUiState> =
        combine(
            habitRepository.observeAllHabits(),
            habitRepository.observeAllCompletions(),
            observeHabitStreaks(),
        ) { habits, completions, streaks ->
            val today = timeProvider.today()
            val todayCompletions = completions
                .filter { it.date == today }
                .groupingBy { it.habitId }
                .eachCount()

            val items = habits.map { it.toUi(today, todayCompletions, streaks) }
            HabitsUiState(
                isLoading = false,
                todayHabits = items.filter { !it.archived && it.scheduledToday },
                otherHabits = items.filter { !it.archived && !it.scheduledToday },
                archivedHabits = items.filter { it.archived },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HabitsUiState(),
        )

    override fun onAction(action: HabitsUiAction) {
        when (action) {
            is HabitsUiAction.ToggleHabit -> toggle(action.habitId, action.completed)
            is HabitsUiAction.SetArchived -> setArchived(action.habitId, action.archived)
            is HabitsUiAction.DeleteHabit -> delete(action.habitId)
        }
    }

    private fun toggle(
        habitId: String,
        completed: Boolean,
    ) {
        val today = timeProvider.today()
        viewModelScope.launch {
            if (completed) habitRepository.complete(habitId, today) else habitRepository.undoComplete(habitId, today)
        }
    }

    private fun setArchived(
        habitId: String,
        archived: Boolean,
    ) {
        viewModelScope.launch { habitRepository.setArchived(habitId, archived) }
    }

    private fun delete(habitId: String) {
        viewModelScope.launch { habitRepository.deleteHabit(habitId) }
    }

    private fun Habit.toUi(
        today: LocalDate,
        todayCompletions: Map<String, Int>,
        streaks: Map<String, HabitStreaks>,
    ): HabitListItemUi {
        val done = todayCompletions[id] ?: 0
        val streak = streaks[id]?.current?.takeIf { it > 0 }?.let { "$it day streak" }
        return HabitListItemUi(
            id = id,
            name = name,
            icon = icon,
            accent = color,
            subtitle = listOfNotNull("$done/$targetPerDay", streak).joinToString(SEPARATOR),
            completed = done >= targetPerDay,
            scheduledToday = schedule.isScheduledOn(today),
            archived = archived,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val SEPARATOR = " · "
    }
}