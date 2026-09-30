package ru.alexey.flowapp.core.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BackupDto(
    @SerialName("version") val version: Int,
    @SerialName("exported_at") val exportedAt: Long,
    @SerialName("settings") val settings: SettingsDto,
    @SerialName("categories") val categories: List<CategoryDto> = emptyList(),
    @SerialName("tasks") val tasks: List<TaskDto> = emptyList(),
    @SerialName("habits") val habits: List<HabitDto> = emptyList(),
    @SerialName("habit_completions") val habitCompletions: List<HabitCompletionDto> = emptyList(),
    @SerialName("focus_sessions") val focusSessions: List<FocusSessionDto> = emptyList(),
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
internal data class SettingsDto(
    @SerialName("theme_mode") val themeMode: String,
    @SerialName("accent_color") val accentColor: String,
    @SerialName("focus_minutes") val focusMinutes: Int,
    @SerialName("short_break_minutes") val shortBreakMinutes: Int,
    @SerialName("long_break_minutes") val longBreakMinutes: Int,
    @SerialName("start_of_week") val startOfWeek: Int,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean,
)

@Serializable
internal data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("color") val color: String,
    @SerialName("icon") val icon: String,
    @SerialName("sort_order") val sortOrder: Int,
)

@Serializable
internal data class TaskDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("estimate_seconds") val estimateSeconds: Long? = null,
    @SerialName("focused_seconds") val focusedSeconds: Long = 0,
    @SerialName("due_date") val dueDate: Long? = null,
    @SerialName("priority") val priority: String,
    @SerialName("status") val status: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("completed_at") val completedAt: Long? = null,
)

@Serializable
internal data class HabitDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("icon") val icon: String,
    @SerialName("color") val color: String,
    @SerialName("schedule_type") val scheduleType: String,
    @SerialName("schedule_days_mask") val scheduleDaysMask: Int,
    @SerialName("target_per_day") val targetPerDay: Int,
    @SerialName("reminder_minute_of_day") val reminderMinuteOfDay: Int? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("archived") val archived: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
internal data class HabitCompletionDto(
    @SerialName("id") val id: String,
    @SerialName("habit_id") val habitId: String,
    @SerialName("date") val date: Long,
    @SerialName("completed_at") val completedAt: Long,
)

@Serializable
internal data class FocusSessionDto(
    @SerialName("id") val id: String,
    @SerialName("task_id") val taskId: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("kind") val kind: String,
    @SerialName("planned_seconds") val plannedSeconds: Long,
    @SerialName("actual_seconds") val actualSeconds: Long,
    @SerialName("started_at") val startedAt: Long,
    @SerialName("ended_at") val endedAt: Long,
    @SerialName("completed") val completed: Boolean,
)