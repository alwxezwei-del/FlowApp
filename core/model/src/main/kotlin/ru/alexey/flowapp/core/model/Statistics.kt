package ru.alexey.flowapp.core.model

import kotlinx.datetime.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

/**
 * Statistics period
 *
 * @param key stable storage key
 */
enum class StatisticsPeriod(
    val key: String,
) {
    WEEK("week"),
    MONTH("month"),
    YEAR("year"),
    ;

    companion object {
        val Default: StatisticsPeriod = WEEK

        fun parse(key: String?): StatisticsPeriod = entries.firstOrNull { it.key == key } ?: Default
    }
}

/** Inclusive date range */
data class DateRange(
    val start: LocalDate,
    val endInclusive: LocalDate,
) {
    operator fun contains(date: LocalDate): Boolean = date in start..endInclusive
}

/** Focused time for one day */
data class DailyFocus(
    val date: LocalDate,
    val duration: Duration,
)

/**
 * Focused time per category
 *
 * @param categoryId null = uncategorized
 * @param share 0-1f
 */
data class CategoryFocus(
    val categoryId: String?,
    val categoryName: String,
    val color: AccentColor,
    val duration: Duration,
    val share: Float,
)

/**
 * Habit consistency over a period
 *
 * @param completedDays scheduled days with the target reached
 */
data class HabitConsistency(
    val habitId: String,
    val habitName: String,
    val icon: String,
    val color: AccentColor,
    val completedDays: Int,
    val scheduledDays: Int,
) {
    val rate: Float
        get() = if (scheduledDays <= 0) 0f else completedDays.toFloat() / scheduledDays
}

/**
 * Statistics screen summary, computed on the fly from Room
 *
 * @param previousTotalFocus same value for the previous period, for comparison
 */
data class StatisticsSummary(
    val period: StatisticsPeriod,
    val range: DateRange,
    val totalFocus: Duration = ZERO,
    val previousTotalFocus: Duration = ZERO,
    val dailyFocus: List<DailyFocus> = emptyList(),
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val categories: List<CategoryFocus> = emptyList(),
    val habits: List<HabitConsistency> = emptyList(),
) {
    val completionRate: Float
        get() = if (totalTasks <= 0) 0f else completedTasks.toFloat() / totalTasks

    val focusTrend: Float?
        get() {
            val previous = previousTotalFocus.inWholeSeconds
            if (previous <= 0L) return null
            return (totalFocus.inWholeSeconds - previous).toFloat() / previous
        }
}