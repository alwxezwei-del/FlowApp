package ru.alexey.flowapp.core.data.backup

import kotlinx.datetime.DayOfWeek
import ru.alexey.flowapp.core.database.entity.CategoryEntity
import ru.alexey.flowapp.core.database.entity.FocusSessionEntity
import ru.alexey.flowapp.core.database.entity.HabitCompletionEntity
import ru.alexey.flowapp.core.database.entity.HabitEntity
import ru.alexey.flowapp.core.database.entity.TaskEntity
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.AppSettings
import ru.alexey.flowapp.core.model.ThemeMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** Longest interval the timer accepts, same as the custom duration picker */
private const val MAX_DURATION_MINUTES = 180

internal fun CategoryEntity.toDto() = CategoryDto(id, name, color, icon, sortOrder)

internal fun CategoryDto.toEntity() = CategoryEntity(id, name, color, icon, sortOrder)

internal fun TaskEntity.toDto() =
    TaskDto(
        id = id,
        title = title,
        description = description,
        categoryId = categoryId,
        estimateSeconds = estimateSeconds,
        focusedSeconds = focusedSeconds,
        dueDate = dueDate,
        priority = priority,
        status = status,
        createdAt = createdAt,
        completedAt = completedAt,
    )

internal fun TaskDto.toEntity() =
    TaskEntity(
        id = id,
        title = title,
        description = description,
        categoryId = categoryId,
        estimateSeconds = estimateSeconds,
        focusedSeconds = focusedSeconds,
        dueDate = dueDate,
        priority = priority,
        status = status,
        createdAt = createdAt,
        completedAt = completedAt,
    )

internal fun HabitEntity.toDto() =
    HabitDto(
        id = id,
        name = name,
        icon = icon,
        color = color,
        scheduleType = scheduleType,
        scheduleDaysMask = scheduleDaysMask,
        targetPerDay = targetPerDay,
        reminderMinuteOfDay = reminderMinuteOfDay,
        note = note,
        createdAt = createdAt,
        archived = archived,
        sortOrder = sortOrder,
    )

internal fun HabitDto.toEntity() =
    HabitEntity(
        id = id,
        name = name,
        icon = icon,
        color = color,
        scheduleType = scheduleType,
        scheduleDaysMask = scheduleDaysMask,
        targetPerDay = targetPerDay,
        reminderMinuteOfDay = reminderMinuteOfDay,
        note = note,
        createdAt = createdAt,
        archived = archived,
        sortOrder = sortOrder,
    )

internal fun HabitCompletionEntity.toDto() = HabitCompletionDto(id, habitId, date, completedAt)

internal fun HabitCompletionDto.toEntity() = HabitCompletionEntity(id, habitId, date, completedAt)

internal fun FocusSessionEntity.toDto() =
    FocusSessionDto(
        id = id,
        taskId = taskId,
        categoryId = categoryId,
        kind = kind,
        plannedSeconds = plannedSeconds,
        actualSeconds = actualSeconds,
        startedAt = startedAt,
        endedAt = endedAt,
        completed = completed,
    )

internal fun FocusSessionDto.toEntity() =
    FocusSessionEntity(
        id = id,
        taskId = taskId,
        categoryId = categoryId,
        kind = kind,
        plannedSeconds = plannedSeconds,
        actualSeconds = actualSeconds,
        startedAt = startedAt,
        endedAt = endedAt,
        completed = completed,
    )

internal fun AppSettings.toDto() =
    SettingsDto(
        themeMode = themeMode.key,
        accentColor = accentColor.key,
        focusMinutes = focusDuration.inWholeMinutes.toInt(),
        shortBreakMinutes = shortBreakDuration.inWholeMinutes.toInt(),
        longBreakMinutes = longBreakDuration.inWholeMinutes.toInt(),
        startOfWeek = startOfWeek.ordinal,
        notificationsEnabled = notificationsEnabled,
    )

internal fun SettingsDto.toDomain() =
    AppSettings(
        themeMode = ThemeMode.parse(themeMode),
        accentColor = AccentColor.parse(accentColor),
        focusDuration = focusMinutes.minutesOr(AppSettings.DefaultFocusDuration),
        shortBreakDuration = shortBreakMinutes.minutesOr(AppSettings.DefaultShortBreak),
        longBreakDuration = longBreakMinutes.minutesOr(AppSettings.DefaultLongBreak),
        startOfWeek = DayOfWeek.entries.getOrElse(startOfWeek) { DayOfWeek.MONDAY },
        notificationsEnabled = notificationsEnabled,
    )

private fun Int.minutesOr(default: Duration): Duration = if (this in 1..MAX_DURATION_MINUTES) minutes else default

internal fun BackupDto.withValidReferences(): BackupDto {
    val categoryIds = categories.mapTo(HashSet()) { it.id }
    val taskIds = tasks.mapTo(HashSet()) { it.id }
    val habitIds = habits.mapTo(HashSet()) { it.id }

    return copy(
        tasks = tasks.map { task ->
            if (task.categoryId == null || task.categoryId in categoryIds) task else task.copy(categoryId = null)
        },
        habitCompletions = habitCompletions.filter { it.habitId in habitIds },
        focusSessions = focusSessions.map { session ->
            if (session.taskId == null || session.taskId in taskIds) session else session.copy(taskId = null)
        },
    )
}