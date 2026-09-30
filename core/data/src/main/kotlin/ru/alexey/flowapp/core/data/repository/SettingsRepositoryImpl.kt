package ru.alexey.flowapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.data.datastore.SettingsStore
import ru.alexey.flowapp.core.domain.repository.SettingsRepository
import ru.alexey.flowapp.core.model.AppSettings
import ru.alexey.flowapp.core.model.ThemeMode

@Single
internal class SettingsRepositoryImpl(
    private val settingsStore: SettingsStore,
) : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> = settingsStore.settings

    override fun currentThemeMode(): ThemeMode = settingsStore.themeModeBlocking()

    override suspend fun updateSettings(settings: AppSettings) = settingsStore.update(settings)
}