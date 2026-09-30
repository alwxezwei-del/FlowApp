package ru.alexey.flowapp.core.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.alexey.flowapp.core.model.AppSettings
import kotlin.time.Duration.Companion.minutes

class BackupMappersTest {
    private val settings = SettingsDto(
        themeMode = "system",
        accentColor = "purple",
        focusMinutes = 45,
        shortBreakMinutes = 5,
        longBreakMinutes = 15,
        startOfWeek = 0,
        notificationsEnabled = true,
    )

    @Test
    fun `references to missing rows are dropped`() {
        val dto = BackupDto(
            version = BackupDto.CURRENT_VERSION,
            exportedAt = 0,
            settings = settings,
            categories = listOf(CategoryDto("work", "Work", "purple", "work", 0)),
            tasks = listOf(
                task(id = "kept", categoryId = "work"),
                task(id = "orphan", categoryId = "deleted"),
            ),
            habits = listOf(habit("water")),
            habitCompletions = listOf(
                HabitCompletionDto("c1", habitId = "water", date = 1, completedAt = 1),
                HabitCompletionDto("c2", habitId = "deleted", date = 1, completedAt = 1),
            ),
            focusSessions = listOf(
                session(id = "s1", taskId = "kept"),
                session(id = "s2", taskId = "deleted"),
                session(id = "s3", taskId = null),
            ),
        )

        val result = dto.withValidReferences()

        assertEquals("work", result.tasks.first { it.id == "kept" }.categoryId)
        assertNull(result.tasks.first { it.id == "orphan" }.categoryId)
        assertEquals(listOf("c1"), result.habitCompletions.map { it.id })
        assertEquals(listOf("kept", null, null), result.focusSessions.map { it.taskId })
    }

    @Test
    fun `out of range durations fall back to defaults`() {
        val domain = settings.copy(focusMinutes = 0, shortBreakMinutes = -5, longBreakMinutes = 10_000).toDomain()

        assertEquals(AppSettings.DefaultFocusDuration, domain.focusDuration)
        assertEquals(AppSettings.DefaultShortBreak, domain.shortBreakDuration)
        assertEquals(AppSettings.DefaultLongBreak, domain.longBreakDuration)
    }

    @Test
    fun `valid durations are kept`() {
        assertEquals(45.minutes, settings.toDomain().focusDuration)
    }

    private fun task(
        id: String,
        categoryId: String?,
    ) = TaskDto(id = id, title = id, categoryId = categoryId, priority = "medium", status = "active", createdAt = 0)

    private fun habit(id: String) =
        HabitDto(
            id = id,
            name = id,
            icon = "water",
            color = "blue",
            scheduleType = "daily",
            scheduleDaysMask = 0b111_1111,
            targetPerDay = 1,
            createdAt = 0,
        )

    private fun session(
        id: String,
        taskId: String?,
    ) = FocusSessionDto(
        id = id,
        taskId = taskId,
        kind = "focus",
        plannedSeconds = 1500,
        actualSeconds = 1500,
        startedAt = 0,
        endedAt = 0,
        completed = true,
    )
}