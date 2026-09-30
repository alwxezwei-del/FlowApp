package ru.alexey.flowapp.core.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** Current time source, injectable so tests dont depend on the real date */
interface TimeProvider {
    fun now(): Instant

    fun timeZone(): TimeZone

    /** Current date in [timeZone] */
    fun today(): LocalDate = now().toLocalDateTime(timeZone()).date
}

class SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Clock.System.now()

    override fun timeZone(): TimeZone = TimeZone.currentSystemDefault()
}