package ru.alexey.flowapp.core.common

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.StatisticsPeriod

/** Statistics period bounds */
fun statisticsRange(
    period: StatisticsPeriod,
    today: LocalDate,
    startOfWeek: DayOfWeek,
): DateRange =
    when (period) {
        StatisticsPeriod.WEEK -> {
            val start = today.startOfWeek(startOfWeek)
            DateRange(start, start.plus(DatePeriod(days = 6)))
        }

        StatisticsPeriod.MONTH -> {
            val start = LocalDate(today.year, today.month, 1)
            DateRange(start, start.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)))
        }

        StatisticsPeriod.YEAR -> {
            DateRange(LocalDate(today.year, 1, 1), LocalDate(today.year, 12, 31))
        }
    }

/** Previous period of the same length */
fun previousRange(
    period: StatisticsPeriod,
    range: DateRange,
): DateRange =
    when (period) {
        StatisticsPeriod.WEEK -> {
            DateRange(
                range.start.minus(DatePeriod(days = 7)),
                range.endInclusive.minus(DatePeriod(days = 7)),
            )
        }

        StatisticsPeriod.MONTH -> {
            val start = range.start.minus(DatePeriod(months = 1))
            DateRange(start, range.start.minus(DatePeriod(days = 1)))
        }

        StatisticsPeriod.YEAR -> {
            DateRange(
                range.start.minus(DatePeriod(years = 1)),
                range.endInclusive.minus(DatePeriod(years = 1)),
            )
        }
    }

/** Start of the week containing this date */
fun LocalDate.startOfWeek(startOfWeek: DayOfWeek): LocalDate {
    val shift = (dayOfWeek.isoDayNumber - startOfWeek.isoDayNumber + DAYS_IN_WEEK) % DAYS_IN_WEEK
    return minus(DatePeriod(days = shift))
}

/** All days in the range, inclusive */
fun DateRange.days(): List<LocalDate> =
    buildList {
        var current = start
        while (current <= endInclusive) {
            add(current)
            current = current.plus(DatePeriod(days = 1))
        }
    }

/** ISO day number, Monday = 1 */
val DayOfWeek.isoDayNumber: Int get() = ordinal + 1

private const val DAYS_IN_WEEK = 7