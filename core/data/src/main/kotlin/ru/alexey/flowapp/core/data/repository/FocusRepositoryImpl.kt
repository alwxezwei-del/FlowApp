package ru.alexey.flowapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.database.dao.FocusSessionDao
import ru.alexey.flowapp.core.database.entity.toDomain
import ru.alexey.flowapp.core.database.entity.toEntity
import ru.alexey.flowapp.core.domain.repository.FocusRepository
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.FocusSession
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Single
internal class FocusRepositoryImpl(
    private val focusSessionDao: FocusSessionDao,
    private val timeProvider: TimeProvider,
) : FocusRepository {
    override fun observeSessions(range: DateRange): Flow<List<FocusSession>> {
        val (from, to) = range.toMillis(timeProvider.timeZone())
        return focusSessionDao.observeInRange(from, to).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeRecentSessions(limit: Int): Flow<List<FocusSession>> =
        focusSessionDao.observeRecent(limit).map { entities -> entities.map { it.toDomain() } }

    override fun observeTotalFocus(range: DateRange): Flow<Duration> {
        val (from, to) = range.toMillis(timeProvider.timeZone())
        return focusSessionDao.observeTotalSeconds(from, to).map { it.seconds }
    }

    override suspend fun saveSession(session: FocusSession) = focusSessionDao.insert(session.toEntity())

    override suspend fun deleteSession(id: String) = focusSessionDao.deleteById(id)
}

private fun DateRange.toMillis(timeZone: TimeZone): Pair<Long, Long> {
    val from = start.atStartOfDayIn(timeZone)
    val to = endInclusive.atTime(LocalTime(23, 59, 59, 999_000_000)).toInstant(timeZone)
    return from.toEpochMilliseconds() to to.toEpochMilliseconds()
}

private fun LocalDate.atStartOfDayIn(timeZone: TimeZone) = atTime(LocalTime(0, 0)).toInstant(timeZone)