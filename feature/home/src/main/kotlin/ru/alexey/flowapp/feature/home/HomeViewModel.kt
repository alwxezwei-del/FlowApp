package ru.alexey.flowapp.feature.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.common.startOfWeek
import ru.alexey.flowapp.core.domain.repository.HabitRepository
import ru.alexey.flowapp.core.domain.repository.SettingsRepository
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.domain.usecase.HabitStreaks
import ru.alexey.flowapp.core.domain.usecase.ObserveHabitStreaksUseCase
import ru.alexey.flowapp.core.domain.usecase.ObserveTodayUseCase
import ru.alexey.flowapp.core.domain.usecase.TodaySnapshot
import ru.alexey.flowapp.core.model.Category
import ru.alexey.flowapp.core.model.HabitDayProgress
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import ru.alexey.flowapp.core.ui.BaseComposeViewModel
import ru.alexey.flowapp.core.ui.format.formatFocusProgress
import ru.alexey.flowapp.core.ui.format.formatShort

@KoinViewModel
internal class HomeViewModel(
    private val observeToday: ObserveTodayUseCase,
    private val observeHabitStreaks: ObserveHabitStreaksUseCase,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider,
) : BaseComposeViewModel<HomeUiState, HomeUiAction>(initialState = HomeUiState.Loading) {
    private val selectedDate = MutableStateFlow(timeProvider.today())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<HomeUiState> =
        combine(
            selectedDate,
            settingsRepository.observeSettings(),
        ) { date, settings -> date to settings.startOfWeek }
            .flatMapLatest { (date, startOfWeek) ->
                combine(
                    observeToday(date),
                    observeHabitStreaks(),
                ) { snapshot, streaks -> snapshot.toContent(date, startOfWeek, streaks) }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = HomeUiState.Loading,
            )

    override fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.SelectDate -> selectedDate.value = action.date
            is HomeUiAction.ToggleTask -> toggleTask(action.taskId, action.completed)
            is HomeUiAction.ToggleHabit -> toggleHabit(action.habitId, action.completed)
        }
    }

    private fun toggleTask(
        taskId: String,
        completed: Boolean,
    ) {
        viewModelScope.launch {
            taskRepository.setStatus(taskId, if (completed) TaskStatus.COMPLETED else TaskStatus.ACTIVE)
        }
    }

    /** Completion is recorded for the selected day, allowing backfilling past days. */
    private fun toggleHabit(
        habitId: String,
        completed: Boolean,
    ) {
        val date = selectedDate.value
        viewModelScope.launch {
            if (completed) {
                habitRepository.complete(habitId, date)
            } else {
                habitRepository.undoComplete(habitId, date)
            }
        }
    }

    private fun TodaySnapshot.toContent(
        date: LocalDate,
        startOfWeek: kotlinx.datetime.DayOfWeek,
        streaks: Map<String, HabitStreaks>,
    ): HomeUiState.Content {
        val weekStart = date.startOfWeek(startOfWeek)
        val categoriesById = categories.associateBy(Category::id)

        return HomeUiState.Content(
            date = date,
            today = timeProvider.today(),
            weekDays = List(DAYS_IN_WEEK) { weekStart.plus(DatePeriod(days = it)) },
            greeting = currentGreeting(),
            focusTotal = focusToday.formatShort(),
            focusTrendPercent = focusTrend,
            tasks = tasks.map { it.toUi(categoriesById) },
            completedTasks = completedTasks,
            totalTasks = totalTasks,
            habits = habits.map { it.toUi(streaks[it.habit.id]) },
        )
    }

    private fun Task.toUi(categoriesById: Map<String, Category>): TaskUi {
        val category = categoryId?.let(categoriesById::get)
        val parts = listOfNotNull(category?.name, focusedTime.formatFocusProgress(estimate))
        return TaskUi(
            id = id,
            title = title,
            subtitle = parts.takeIf { it.isNotEmpty() }?.joinToString(SEPARATOR),
            completed = isCompleted,
            accent = category?.color ?: ru.alexey.flowapp.core.model.AccentColor.Default,
            progress = focusProgress,
        )
    }

    private fun HabitDayProgress.toUi(streaks: HabitStreaks?): HabitUi =
        HabitUi(
            id = habit.id,
            name = habit.name,
            icon = habit.icon,
            accent = habit.color,
            completedCount = completedCount,
            target = habit.targetPerDay,
            streakDays = streaks?.current ?: 0,
            completed = isDone,
        )

    private fun currentGreeting(): Greeting {
        val hour = timeProvider.now().toLocalDateTime(timeProvider.timeZone()).hour
        return when {
            hour < MORNING_END_HOUR -> Greeting.MORNING
            hour < AFTERNOON_END_HOUR -> Greeting.AFTERNOON
            else -> Greeting.EVENING
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val DAYS_IN_WEEK = 7
        const val SEPARATOR = " · "
        const val MORNING_END_HOUR = 12
        const val AFTERNOON_END_HOUR = 18
    }
}