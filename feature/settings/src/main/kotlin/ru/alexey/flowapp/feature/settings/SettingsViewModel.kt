package ru.alexey.flowapp.feature.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.domain.repository.BackupRepository
import ru.alexey.flowapp.core.domain.repository.ImportResult
import ru.alexey.flowapp.core.domain.repository.SettingsRepository
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.AppSettings
import ru.alexey.flowapp.core.model.ThemeMode
import ru.alexey.flowapp.core.ui.BaseComposeViewModel
import kotlin.time.Duration.Companion.minutes

@KoinViewModel
internal class SettingsViewModel(
    @InjectedParam private val appVersion: String,
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
) : BaseComposeViewModel<SettingsUiState, SettingsUiAction>(
        initialState = SettingsUiState(appVersion = appVersion),
    ) {
    init {
        settingsRepository
            .observeSettings()
            .onEach { settings -> _state.update { it.apply(settings) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.ThemeSelected -> update { it.copy(themeMode = action.mode) }
            is SettingsUiAction.AccentSelected -> update { it.copy(accentColor = action.color) }
            is SettingsUiAction.FocusDurationChanged -> update { it.copy(focusDuration = action.minutes.minutes) }
            is SettingsUiAction.ShortBreakChanged -> update { it.copy(shortBreakDuration = action.minutes.minutes) }
            is SettingsUiAction.LongBreakChanged -> update { it.copy(longBreakDuration = action.minutes.minutes) }
            is SettingsUiAction.StartOfWeekChanged -> update { it.copy(startOfWeek = action.day) }
            is SettingsUiAction.NotificationsToggled -> update { it.copy(notificationsEnabled = action.enabled) }
            SettingsUiAction.ExportRequested -> export()
            is SettingsUiAction.ImportFileRead -> import(action.json)
            SettingsUiAction.ResetRequested -> _state.update { it.copy(resetDialogVisible = true) }
            SettingsUiAction.ResetDismissed -> _state.update { it.copy(resetDialogVisible = false) }
            SettingsUiAction.ResetConfirmed -> reset()
        }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = settingsRepository.observeSettings().first()
            settingsRepository.updateSettings(transform(current))
        }
    }

    private fun export() {
        viewModelScope.launch {
            sendEvent(SettingsUiEvent.ExportReady(backupRepository.export()))
        }
    }

    private fun import(json: String) {
        viewModelScope.launch {
            val event = when (val result = backupRepository.import(json)) {
                is ImportResult.Success -> SettingsUiEvent.Message(
                    messageRes = R.string.settings_import_success,
                    formatArgs = listOf(result.tasks, result.habits, result.sessions),
                )

                ImportResult.InvalidFile -> SettingsUiEvent.Message(R.string.settings_import_invalid)

                is ImportResult.UnsupportedVersion -> SettingsUiEvent.Message(
                    messageRes = R.string.settings_import_unsupported,
                    formatArgs = listOf(result.fileVersion, result.supportedVersion),
                )
            }
            sendEvent(event)
        }
    }

    private fun reset() {
        viewModelScope.launch {
            backupRepository.reset()
            _state.update { it.copy(resetDialogVisible = false) }
            sendEvent(SettingsUiEvent.Message(R.string.settings_reset_done))
        }
    }

    private fun SettingsUiState.apply(settings: AppSettings): SettingsUiState =
        copy(
            isLoading = false,
            themeMode = settings.themeMode,
            accentColor = settings.accentColor,
            focusMinutes = settings.focusDuration.inWholeMinutes.toInt(),
            shortBreakMinutes = settings.shortBreakDuration.inWholeMinutes.toInt(),
            longBreakMinutes = settings.longBreakDuration.inWholeMinutes.toInt(),
            startOfWeek = settings.startOfWeek,
            notificationsEnabled = settings.notificationsEnabled,
            appVersion = appVersion,
        )
}

/** Duration options offered in settings. */
internal object DurationPresets {
    val Focus = listOf(15, 25, 45, 60)
    val ShortBreak = listOf(3, 5, 10)
    val LongBreak = listOf(10, 15, 20, 30)
    val StartOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY)
    val Themes = ThemeMode.entries
    val Accents = AccentColor.entries
}