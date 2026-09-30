package ru.alexey.flowapp.core.ui.format

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.time.Duration

fun Duration.formatShort(): String {
    val totalMinutes = inWholeMinutes
    val hours = totalMinutes / MINUTES_IN_HOUR
    val minutes = totalMinutes % MINUTES_IN_HOUR
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        totalMinutes > 0 -> "${minutes}m"
        else -> "${inWholeSeconds}s"
    }
}

// `12m / 45m` with an estimate, `12m` without one, `45m` if nothing focused yet
fun Duration.formatFocusProgress(estimate: Duration?): String? {
    val focused = takeIf { it.isPositive() }?.formatShort()
    val planned = estimate?.formatShort()
    return when {
        focused != null && planned != null -> "$focused / $planned"
        else -> focused ?: planned
    }
}

fun Duration.formatTimer(): String {
    val totalSeconds = inWholeSeconds.coerceAtLeast(0)
    val hours = totalSeconds / SECONDS_IN_HOUR
    val minutes = (totalSeconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
    val seconds = totalSeconds % SECONDS_IN_MINUTE
    return if (hours > 0) {
        "$hours:${minutes.pad()}:${seconds.pad()}"
    } else {
        "${minutes.pad()}:${seconds.pad()}"
    }
}

fun Float.formatSignedPercent(): String {
    val percent = (this * PERCENT).roundToInt()
    val sign = if (percent >= 0) "+" else "-"
    return "$sign${percent.absoluteValue}%"
}

fun Float.formatPercent(): String = "${(this * PERCENT).roundToInt()}%"

/** Time of day: 19:00 */
fun LocalTime.formatTime(): String = TimeFormat.format(this)

/** Full date: Wednesday, Sep 30 */
fun LocalDate.formatFullDate(): String = FullDateFormat.format(this)

/** Short date: Mon, 12 May */
fun LocalDate.formatShortDate(): String = ShortDateFormat.format(this)

fun LocalDate.dayInitial(): String = dayOfWeek.name.take(1)

private fun Long.pad(): String = toString().padStart(2, '0')

private val TimeFormat = LocalTime.Format {
    hour(Padding.ZERO)
    char(':')
    minute(Padding.ZERO)
}

private val FullDateFormat = LocalDate.Format {
    dayOfWeek(kotlinx.datetime.format.DayOfWeekNames.ENGLISH_FULL)
    chars(", ")
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day(Padding.NONE)
}

private val ShortDateFormat = LocalDate.Format {
    dayOfWeek(kotlinx.datetime.format.DayOfWeekNames.ENGLISH_ABBREVIATED)
    chars(", ")
    day(Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
}

private const val MINUTES_IN_HOUR = 60L
private const val SECONDS_IN_MINUTE = 60L
private const val SECONDS_IN_HOUR = 3600L
private const val PERCENT = 100