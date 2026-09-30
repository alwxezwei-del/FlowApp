package ru.alexey.flowapp.feature.focus

import androidx.compose.runtime.Immutable
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiEvent
import ru.alexey.flowapp.core.ui.UiState

@Immutable
data class FocusPresetUi(
    val kind: FocusKind,
    val minutes: Int,
)

/**
 * Task in the picker
 */
@Immutable
data class FocusTaskUi(
    val id: String,
    val title: String,
    val subtitle: String?,
)

/**
 * Timer screen state
 */
data class FocusUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val timeLabel: String = "",
    val progress: Float = 0f,
    val kind: FocusKind = FocusKind.FOCUS,
    val selectedTaskId: String? = null,
    val selectedTaskTitle: String? = null,
    val tasks: List<FocusTaskUi> = emptyList(),
    val presets: List<FocusPresetUi> = emptyList(),
    val selectedMinutes: Int = 0,
    val taskPickerVisible: Boolean = false,
    val durationPickerVisible: Boolean = false,
) : UiState {
    val isActive: Boolean get() = isRunning || isPaused
}

/** Timer screen actions */
sealed interface FocusUiAction : UiAction {
    data class SelectPreset(
        val preset: FocusPresetUi,
    ) : FocusUiAction

    data class SelectCustomDuration(
        val minutes: Int,
    ) : FocusUiAction

    data class SelectTask(
        val taskId: String?,
    ) : FocusUiAction

    data object OpenTaskPicker : FocusUiAction

    data object OpenDurationPicker : FocusUiAction

    data object DismissPicker : FocusUiAction

    data object Start : FocusUiAction

    data object TogglePause : FocusUiAction

    data object Stop : FocusUiAction
}

/**
 * Timer screen events
 */
sealed interface FocusUiEvent : UiEvent {
    data object StartService : FocusUiEvent

    data object StopService : FocusUiEvent
}