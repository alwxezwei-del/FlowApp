package ru.alexey.flowapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alexey.flowapp.core.model.AppSettings
import ru.alexey.flowapp.core.model.ThemeMode

/** App settings stored in DataStore */
interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>

    /** Blocking theme read before the first frame to avoid a theme flash */
    fun currentThemeMode(): ThemeMode

    suspend fun updateSettings(settings: AppSettings)
}