package ru.alexey.flowapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.database.dao.HabitDao
import ru.alexey.flowapp.core.database.entity.toDomain
import ru.alexey.flowapp.core.database.entity.toEntity
import ru.alexey.flowapp.core.domain.repository.HabitRepository
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitCompletion
import ru.alexey.flowapp.core.model.HabitDayProgress
import ru.alexey.flowapp.core.model.newId

@Single
internal class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val timeProvider: TimeProvider,
) : HabitRepository {
    override fun observeHabits(): Flow<List<Habit>> = habitDao.observeActive().map { entities -> entities.map { it.toDomain() } }

    override fun observeAllHabits(): Flow<List<Habit>> = habitDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeHabit(id: String): Flow<Habit?> = habitDao.observeById(id).map { it?.toDomain() }

    override fun observeProgressForDate(date: LocalDate): Flow<List<HabitDayProgress>> =
        combine(
            habitDao.observeScheduledOn(date.dayBit()),
            habitDao.observeCompletionsForDate(date.epochDay()),
        ) { habits, completions ->
            val countByHabit = completions.groupingBy { it.habitId }.eachCount()
            habits.map { entity ->
                val habit = entity.toDomain()
                HabitDayProgress(
                    habit = habit,
                    date = date,
                    completedCount = countByHabit[habit.id] ?: 0,
                    scheduled = true,
                )
            }
        }

    override fun observeCompletions(range: DateRange): Flow<List<HabitCompletion>> =
        habitDao
            .observeCompletionsInRange(range.start.epochDay(), range.endInclusive.epochDay())
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeCompletions(habitId: String): Flow<List<HabitCompletion>> =
        habitDao.observeCompletionsForHabit(habitId).map { entities -> entities.map { it.toDomain() } }

    override fun observeAllCompletions(): Flow<List<HabitCompletion>> =
        habitDao.observeAllCompletions().map { entities -> entities.map { it.toDomain() } }

    override suspend fun createHabit(habit: Habit) = habitDao.insert(habit.toEntity())

    override suspend fun updateHabit(habit: Habit) = habitDao.update(habit.toEntity())

    override suspend fun complete(
        habitId: String,
        date: LocalDate,
    ) {
        habitDao.insertCompletion(
            HabitCompletion(
                id = newId(),
                habitId = habitId,
                date = date,
                completedAt = timeProvider.now(),
            ).toEntity(),
        )
    }

    override suspend fun undoComplete(
        habitId: String,
        date: LocalDate,
    ) = habitDao.deleteLastCompletion(habitId, date.epochDay())

    override suspend fun setArchived(
        id: String,
        archived: Boolean,
    ) = habitDao.updateArchived(id, archived)

    override suspend fun deleteHabit(id: String) = habitDao.deleteById(id)
}

internal fun LocalDate.dayBit(): Int = 1 shl dayOfWeek.ordinal