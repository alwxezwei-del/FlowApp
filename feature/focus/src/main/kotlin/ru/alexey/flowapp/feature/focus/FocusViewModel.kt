package ru.alexey.flowapp.feature.focus

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.CategoryRepository
import ru.alexey.flowapp.core.domain.repository.FocusTimerController
import ru.alexey.flowapp.core.domain.repository.SettingsRepository
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.AppSettings
import ru.alexey.flowapp.core.model.Category
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.RunningSession
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import ru.alexey.flowapp.core.model.TimerState
import ru.alexey.flowapp.core.model.progressAt
import ru.alexey.flowapp.core.model.remainingAt
import ru.alexey.flowapp.core.ui.BaseComposeViewModel
import ru.alexey.flowapp.core.ui.format.formatShort
import ru.alexey.flowapp.core.ui.format.formatTimer
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Timer screen ViewModel. Remaining time isn't stored: each tick recomputes it from controller
 * state and the clock.
 *
 * @param preselectedTaskId task chosen via "Start focus" elsewhere
 */
@KoinViewModel
internal class FocusViewModel(
    @InjectedParam private val preselectedTaskId: String?,
    private val controller: FocusTimerController,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider,
) : BaseComposeViewModel<FocusUiState, FocusUiAction>(initialState = FocusUiState()) {
    private val selectedTaskId = MutableStateFlow(preselectedTaskId)
    private val selectedMinutes = MutableStateFlow<Int?>(null)
    private val selectedKind = MutableStateFlow(FocusKind.FOCUS)
    private val pickerState = MutableStateFlow(PickerState.NONE)

    /** Ticks only while subscribed; in background the service updates the notification. */
    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(TICK_MILLIS)
        }
    }

    override val state: StateFlow<FocusUiState> =
        combine(
            controller.state,
            activeTasks(),
            settingsRepository.observeSettings(),
            combine(selectedTaskId, selectedMinutes, selectedKind, pickerState, ::Selection),
            ticker,
        ) { timerState, tasks, settings, selection, _ ->
            buildState(timerState, tasks, settings, selection)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = FocusUiState(),
        )

    init {
        viewModelScope.launch {
            controller.restore()
            // The interval may have elapsed while the process was dead, finish it on return
            controller.finishIfElapsed()
        }
    }

    override fun onAction(action: FocusUiAction) {
        when (action) {
            is FocusUiAction.SelectPreset -> {
                selectedKind.value = action.preset.kind
                selectedMinutes.value = action.preset.minutes
            }

            is FocusUiAction.SelectCustomDuration -> {
                selectedKind.value = FocusKind.FOCUS
                selectedMinutes.value = action.minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
                pickerState.value = PickerState.NONE
            }

            is FocusUiAction.SelectTask -> {
                selectedTaskId.value = action.taskId
                pickerState.value = PickerState.NONE
            }

            FocusUiAction.OpenTaskPicker -> {
                pickerState.value = PickerState.TASK
            }

            FocusUiAction.OpenDurationPicker -> {
                pickerState.value = PickerState.DURATION
            }

            FocusUiAction.DismissPicker -> {
                pickerState.value = PickerState.NONE
            }

            FocusUiAction.Start -> {
                start()
            }

            FocusUiAction.TogglePause -> {
                togglePause()
            }

            FocusUiAction.Stop -> {
                stop()
            }
        }
    }

    private fun start() {
        val current = state.value
        viewModelScope.launch {
            val task = current.selectedTaskId?.let { taskRepository.getTask(it) }
            controller.start(
                RunningSession(
                    taskId = task?.id,
                    taskTitle = task?.title,
                    categoryId = task?.categoryId,
                    // Breaks aren't counted towards a task, so a session with a task is always focus
                    kind = if (task != null) FocusKind.FOCUS else current.kind,
                    plannedDuration = current.selectedMinutes.minutes,
                    startedAt = timeProvider.now(),
                ),
            )
            sendEvent(FocusUiEvent.StartService)
        }
    }

    private fun togglePause() {
        viewModelScope.launch {
            when (controller.state.value) {
                is TimerState.Running -> controller.pause()
                is TimerState.Paused -> controller.resume()
                TimerState.Idle -> Unit
            }
        }
    }

    private fun stop() {
        viewModelScope.launch {
            controller.stop(completed = false)
            sendEvent(FocusUiEvent.StopService)
        }
    }

    private fun activeTasks() =
        combine(
            taskRepository.observeTasks(),
            categoryRepository.observeCategories(),
        ) { tasks, categories ->
            val categoriesById = categories.associateBy(Category::id)
            tasks
                .filter { it.status == TaskStatus.ACTIVE }
                .map { it.toUi(categoriesById) }
        }

    private fun buildState(
        timerState: TimerState,
        tasks: List<FocusTaskUi>,
        settings: AppSettings,
        selection: Selection,
    ): FocusUiState {
        val presets = settings.toPresets()
        val now = timeProvider.now()
        val session = when (timerState) {
            TimerState.Idle -> null
            is TimerState.Running -> timerState.session
            is TimerState.Paused -> timerState.session
        }

        // While a session is active, show its params instead of the picker selection
        val kind = session?.kind ?: selection.kind
        val minutes = session?.plannedDuration?.inWholeMinutes?.toInt()
            ?: selection.minutes
            ?: presets.first { it.kind == kind }.minutes
        val taskId = session?.taskId ?: selection.taskId

        return FocusUiState(
            isRunning = timerState is TimerState.Running,
            isPaused = timerState is TimerState.Paused,
            timeLabel = if (session != null) {
                timerState.remainingAt(now).formatTimer()
            } else {
                minutes.minutes.formatTimer()
            },
            progress = timerState.progressAt(now),
            kind = kind,
            selectedTaskId = taskId,
            selectedTaskTitle = session?.taskTitle ?: tasks.firstOrNull { it.id == taskId }?.title,
            tasks = tasks,
            presets = presets,
            selectedMinutes = minutes,
            taskPickerVisible = selection.picker == PickerState.TASK,
            durationPickerVisible = selection.picker == PickerState.DURATION,
        )
    }

    private fun AppSettings.toPresets(): List<FocusPresetUi> =
        listOf(
            FocusPresetUi(FocusKind.FOCUS, focusDuration.inWholeMinutes.toInt()),
            FocusPresetUi(FocusKind.SHORT_BREAK, shortBreakDuration.inWholeMinutes.toInt()),
            FocusPresetUi(FocusKind.LONG_BREAK, longBreakDuration.inWholeMinutes.toInt()),
        )

    private fun Task.toUi(categoriesById: Map<String, Category>): FocusTaskUi {
        val category = categoryId?.let(categoriesById::get)
        val parts = listOfNotNull(category?.name, estimate?.formatShort())
        return FocusTaskUi(
            id = id,
            title = title,
            subtitle = parts.takeIf { it.isNotEmpty() }?.joinToString(" · "),
        )
    }

    /** Which modal picker is open. */
    private enum class PickerState { NONE, TASK, DURATION }

    /** User selection before start. Grouped into one object because [combine] has limited arity. */
    private data class Selection(
        val taskId: String?,
        val minutes: Int?,
        val kind: FocusKind,
        val picker: PickerState,
    )

    private companion object {
        val TICK_MILLIS = 1.seconds.inWholeMilliseconds
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val MIN_MINUTES = 1
        const val MAX_MINUTES = 180
    }
}