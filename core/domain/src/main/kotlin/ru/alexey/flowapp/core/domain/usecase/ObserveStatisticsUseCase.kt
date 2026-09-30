package ru.alexey.flowapp.core.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.common.days
import ru.alexey.flowapp.core.common.previousRange
import ru.alexey.flowapp.core.common.statisticsRange
import ru.alexey.flowapp.core.domain.repository.CategoryRepository
import ru.alexey.flowapp.core.domain.repository.FocusRepository
import ru.alexey.flowapp.core.domain.repository.HabitRepository
import ru.alexey.flowapp.core.domain.repository.SettingsRepository
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.Category
import ru.alexey.flowapp.core.model.CategoryFocus
import ru.alexey.flowapp.core.model.DailyFocus
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.FocusSession
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitCompletion
import ru.alexey.flowapp.core.model.HabitConsistency
import ru.alexey.flowapp.core.model.StatisticsPeriod
import ru.alexey.flowapp.core.model.StatisticsSummary
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

/**
 * Builds the statistics summary
 */
@Factory
class ObserveStatisticsUseCase(
    private val settingsRepository: SettingsRepository,
    private val focusRepository: FocusRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val categoryRepository: CategoryRepository,
    private val timeProvider: TimeProvider,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(period: StatisticsPeriod): Flow<StatisticsSummary> =
        settingsRepository
            .observeSettings()
            .map { it.startOfWeek }
            .flatMapLatest { startOfWeek ->
                val range = statisticsRange(period, timeProvider.today(), startOfWeek)
                val previous = previousRange(period, range)

                combine(
                    focusRepository.observeSessions(range),
                    focusRepository.observeTotalFocus(previous),
                    taskRepository.observeTasksInRange(range),
                    habitRepository.observeAllHabits(),
                    combine(
                        habitRepository.observeCompletions(range),
                        categoryRepository.observeCategories(),
                    ) { completions, categories -> completions to categories },
                ) { sessions, previousFocus, tasks, habits, (completions, categories) ->
                    buildSummary(
                        period = period,
                        range = range,
                        sessions = sessions,
                        previousFocus = previousFocus,
                        tasks = tasks,
                        habits = habits,
                        completions = completions,
                        categories = categories,
                    )
                }
            }

    private fun buildSummary(
        period: StatisticsPeriod,
        range: DateRange,
        sessions: List<FocusSession>,
        previousFocus: Duration,
        tasks: List<Task>,
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        categories: List<Category>,
    ): StatisticsSummary {
        val focusSessions = sessions.filter { it.kind == FocusKind.FOCUS }
        val totalFocus = focusSessions.fold(ZERO) { acc, session -> acc + session.actualDuration }

        return StatisticsSummary(
            period = period,
            range = range,
            totalFocus = totalFocus,
            previousTotalFocus = previousFocus,
            dailyFocus = dailyFocus(range, focusSessions),
            completedTasks = tasks.count { it.status == TaskStatus.COMPLETED },
            totalTasks = tasks.count { it.status != TaskStatus.ARCHIVED },
            categories = categoryFocus(focusSessions, categories, totalFocus),
            habits = habitConsistency(range, habits, completions),
        )
    }

    /** Explicit zeros for days */
    private fun dailyFocus(
        range: DateRange,
        sessions: List<FocusSession>,
    ): List<DailyFocus> {
        val byDay: Map<LocalDate, Duration> = sessions
            .groupBy { it.endedAt.toLocalDateTime(timeProvider.timeZone()).date }
            .mapValues { (_, daySessions) -> daySessions.fold(ZERO) { acc, it -> acc + it.actualDuration } }

        return range.days().map { day -> DailyFocus(day, byDay[day] ?: ZERO) }
    }

    private fun categoryFocus(
        sessions: List<FocusSession>,
        categories: List<Category>,
        totalFocus: Duration,
    ): List<CategoryFocus> {
        if (sessions.isEmpty()) return emptyList()
        val categoriesById = categories.associateBy { it.id }
        val totalSeconds = totalFocus.inWholeSeconds

        return sessions
            .groupBy { it.categoryId }
            .map { (categoryId, categorySessions) ->
                val duration = categorySessions.fold(ZERO) { acc, it -> acc + it.actualDuration }
                val category = categoryId?.let(categoriesById::get)
                CategoryFocus(
                    categoryId = categoryId,
                    categoryName = category?.name ?: UNCATEGORIZED,
                    color = category?.color ?: AccentColor.Default,
                    duration = duration,
                    share = if (totalSeconds <= 0L) 0f else duration.inWholeSeconds.toFloat() / totalSeconds,
                )
            }.sortedByDescending { it.duration }
    }

    private fun habitConsistency(
        range: DateRange,
        habits: List<Habit>,
        completions: List<HabitCompletion>,
    ): List<HabitConsistency> {
        if (habits.isEmpty()) return emptyList()
        val days = range.days()
        val completionsByHabit = completions.groupBy { it.habitId }

        return habits
            .map { habit ->
                val doneDays = completionsByHabit[habit.id]
                    .orEmpty()
                    .groupingBy { it.date }
                    .eachCount()
                    .filterValues { it >= habit.targetPerDay }
                    .keys
                val scheduled = days.filter { habit.schedule.isScheduledOn(it) }

                HabitConsistency(
                    habitId = habit.id,
                    habitName = habit.name,
                    icon = habit.icon,
                    color = habit.color,
                    completedDays = scheduled.count { it in doneDays },
                    scheduledDays = scheduled.size,
                )
            }.filter { it.scheduledDays > 0 }
            .sortedByDescending { it.rate }
    }

    private companion object {
        const val UNCATEGORIZED = "No category"
    }
}