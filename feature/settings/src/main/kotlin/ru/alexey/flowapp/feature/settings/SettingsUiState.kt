package ru.alexey.flowapp.feature.settings

import kotlinx.datetime.DayOfWeek
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.ThemeMode
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiEvent
import ru.alexey.flowapp.core.ui.UiState

/**
 * Settings screen state
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.Default,
    val accentColor: AccentColor = AccentColor.Default,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val startOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val notificationsEnabled: Boolean = true,
    val appVersion: String = "",
    val resetDialogVisible: Boolean = false,
) : UiState

/** Settings actions. */
sealed interface SettingsUiAction : UiAction {
    data class ThemeSelected(
        val mode: ThemeMode,
    ) : SettingsUiAction

    data class AccentSelected(
        val color: AccentColor,
    ) : SettingsUiAction

    data class FocusDurationChanged(
        val minutes: Int,
    ) : SettingsUiAction

    data class ShortBreakChanged(
        val minutes: Int,
    ) : SettingsUiAction

    data class LongBreakChanged(
        val minutes: Int,
    ) : SettingsUiAction

    data class StartOfWeekChanged(
        val day: DayOfWeek,
    ) : SettingsUiAction

    data class NotificationsToggled(
        val enabled: Boolean,
    ) : SettingsUiAction

    data object ExportRequested : SettingsUiAction

    /** @param json selected file contents */
    data class ImportFileRead(
        val json: String,
    ) : SettingsUiAction

    data object ResetRequested : SettingsUiAction

    data object ResetConfirmed : SettingsUiAction

    data object ResetDismissed : SettingsUiAction
}

/**
 * Settings events. File I/O stays in the composable (it has a `Context`); the ViewModel only deals
 * with JSON strings.
 */
sealed interface SettingsUiEvent : UiEvent {
    /** @param json export file contents */
    data class ExportReady(
        val json: String,
    ) : SettingsUiEvent

    data class Message(
        val messageRes: Int,
        val formatArgs: List<Any> = emptyList(),
    ) : SettingsUiEvent
}