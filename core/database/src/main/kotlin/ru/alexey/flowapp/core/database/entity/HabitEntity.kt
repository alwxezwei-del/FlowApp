package ru.alexey.flowapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.DayOfWeek
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitSchedule
import kotlin.time.Instant

/** Habit row */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "icon")
    val icon: String,
    @ColumnInfo(name = "color")
    val color: String,
    @ColumnInfo(name = "schedule_type")
    val scheduleType: String,
    @ColumnInfo(name = "schedule_days_mask")
    val scheduleDaysMask: Int,
    @ColumnInfo(name = "target_per_day")
    val targetPerDay: Int,
    @ColumnInfo(name = "reminder_minute_of_day")
    val reminderMinuteOfDay: Int?,
    @ColumnInfo(name = "note")
    val note: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "archived")
    val archived: Boolean,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
) {
    companion object {
        const val SCHEDULE_DAILY = "daily"
        const val SCHEDULE_SELECTED_DAYS = "selected_days"

        /** Mask with all seven days set. */
        const val ALL_DAYS_MASK = 0b111_1111
    }
}

fun HabitEntity.toDomain(): Habit =
    Habit(
        id = id,
        name = name,
        icon = icon,
        color = AccentColor.parse(color),
        schedule = when (scheduleType) {
            HabitEntity.SCHEDULE_SELECTED_DAYS -> HabitSchedule.SelectedDays(scheduleDaysMask.toDays())
            else -> HabitSchedule.Daily
        },
        targetPerDay = targetPerDay.coerceAtLeast(1),
        reminderMinuteOfDay = reminderMinuteOfDay,
        note = note,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        archived = archived,
        sortOrder = sortOrder,
    )

fun Habit.toEntity(): HabitEntity =
    when (val schedule = schedule) {
        HabitSchedule.Daily -> toEntity(HabitEntity.SCHEDULE_DAILY, HabitEntity.ALL_DAYS_MASK)
        is HabitSchedule.SelectedDays -> toEntity(HabitEntity.SCHEDULE_SELECTED_DAYS, schedule.days.toMask())
    }

private fun Habit.toEntity(
    scheduleType: String,
    daysMask: Int,
): HabitEntity =
    HabitEntity(
        id = id,
        name = name,
        icon = icon,
        color = color.key,
        scheduleType = scheduleType,
        scheduleDaysMask = daysMask,
        targetPerDay = targetPerDay.coerceAtLeast(1),
        reminderMinuteOfDay = reminderMinuteOfDay,
        note = note,
        createdAt = createdAt.toEpochMilliseconds(),
        archived = archived,
        sortOrder = sortOrder,
    )

/** Monday = lowest bit. */
fun Set<DayOfWeek>.toMask(): Int = fold(0) { mask, day -> mask or (1 shl day.ordinal) }

fun Int.toDays(): Set<DayOfWeek> = DayOfWeek.entries.filterTo(mutableSetOf()) { this and (1 shl it.ordinal) != 0 }