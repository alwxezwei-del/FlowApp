package ru.alexey.flowapp.core.model

import kotlinx.datetime.DayOfWeek
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Theme mode
 *
 * @param key stable storage key
 */
enum class ThemeMode(
    val key: String,
) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        val Default: ThemeMode = SYSTEM

        fun parse(key: String?): ThemeMode = entries.firstOrNull { it.key == key } ?: Default
    }
}

/**
 * User settings
 *
 * @param startOfWeek used by calendar and statistics
 * @param notificationsEnabled timer notifications and habit reminders
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.Default,
    val accentColor: AccentColor = AccentColor.Default,
    val focusDuration: Duration = DefaultFocusDuration,
    val shortBreakDuration: Duration = DefaultShortBreak,
    val longBreakDuration: Duration = DefaultLongBreak,
    val startOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val notificationsEnabled: Boolean = true,
) {
    companion object {
        val DefaultFocusDuration: Duration = 25.minutes
        val DefaultShortBreak: Duration = 5.minutes
        val DefaultLongBreak: Duration = 15.minutes

        /** Focus duration presets */
        val FocusPresets: List<Duration> = listOf(25.minutes, 45.minutes, 60.minutes)
    }
}