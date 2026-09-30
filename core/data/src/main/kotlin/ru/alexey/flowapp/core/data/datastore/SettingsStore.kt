package ru.alexey.flowapp.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.AppSettings
import ru.alexey.flowapp.core.model.ThemeMode
import java.io.IOException
import kotlin.time.Duration.Companion.minutes

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** App settings in DataStore */
@Single
class SettingsStore(
    private val context: Context,
) {
    private val dataStore get() = context.settingsDataStore

    /** Falls back to defaults on read errors */
    val settings: Flow<AppSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toSettings() }

    /** Blocking theme read, called once */
    fun themeModeBlocking(): ThemeMode =
        runBlocking {
            runCatching { ThemeMode.parse(dataStore.data.first()[KeyThemeMode]) }.getOrDefault(ThemeMode.Default)
        }

    suspend fun update(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[KeyThemeMode] = settings.themeMode.key
            prefs[KeyAccentColor] = settings.accentColor.key
            prefs[KeyFocusMinutes] = settings.focusDuration.inWholeMinutes.toInt()
            prefs[KeyShortBreakMinutes] = settings.shortBreakDuration.inWholeMinutes.toInt()
            prefs[KeyLongBreakMinutes] = settings.longBreakDuration.inWholeMinutes.toInt()
            prefs[KeyStartOfWeek] = settings.startOfWeek.ordinal
            prefs[KeyNotifications] = settings.notificationsEnabled
        }
    }

    /** Resets settings to defaults */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun Preferences.toSettings(): AppSettings =
        AppSettings(
            themeMode = ThemeMode.parse(this[KeyThemeMode]),
            accentColor = AccentColor.parse(this[KeyAccentColor]),
            focusDuration = (this[KeyFocusMinutes] ?: DEFAULT_FOCUS_MINUTES).minutes,
            shortBreakDuration = (this[KeyShortBreakMinutes] ?: DEFAULT_SHORT_BREAK_MINUTES).minutes,
            longBreakDuration = (this[KeyLongBreakMinutes] ?: DEFAULT_LONG_BREAK_MINUTES).minutes,
            startOfWeek = DayOfWeek.entries.getOrElse(this[KeyStartOfWeek] ?: 0) { DayOfWeek.MONDAY },
            notificationsEnabled = this[KeyNotifications] ?: true,
        )

    private companion object {
        val KeyThemeMode = stringPreferencesKey("theme_mode")
        val KeyAccentColor = stringPreferencesKey("accent_color")
        val KeyFocusMinutes = intPreferencesKey("focus_minutes")
        val KeyShortBreakMinutes = intPreferencesKey("short_break_minutes")
        val KeyLongBreakMinutes = intPreferencesKey("long_break_minutes")
        val KeyStartOfWeek = intPreferencesKey("start_of_week")
        val KeyNotifications = booleanPreferencesKey("notifications_enabled")

        const val DEFAULT_FOCUS_MINUTES = 25
        const val DEFAULT_SHORT_BREAK_MINUTES = 5
        const val DEFAULT_LONG_BREAK_MINUTES = 15
    }
}