package ru.alexey.flowapp.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.alexey.flowapp.core.domain.fake.FakeCategoryRepository
import ru.alexey.flowapp.core.domain.fake.FakeFocusRepository
import ru.alexey.flowapp.core.domain.fake.FakeHabitRepository
import ru.alexey.flowapp.core.domain.fake.FakeSettingsRepository
import ru.alexey.flowapp.core.domain.fake.FakeTaskRepository
import ru.alexey.flowapp.core.domain.fake.FakeTimeProvider
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.AppSettings
import ru.alexey.flowapp.core.model.Category
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.FocusSession
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitCompletion
import ru.alexey.flowapp.core.model.HabitSchedule
import ru.alexey.flowapp.core.model.StatisticsPeriod
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Statistics aggregation tests: the summary is derived from the same data as other screens. */
class ObserveStatisticsUseCaseTest {
    // Wednesday, mid-week: shows the week starts from the configured first day, not from the date
    private val today = LocalDate(2026, 9, 30)
    private val timeProvider = FakeTimeProvider(today)

    private val workCategory = Category(id = "work", name = "Work", color = AccentColor.PURPLE, icon = "work")
    private val learningCategory =
        Category(id = "learning", name = "Learning", color = AccentColor.BLUE, icon = "learning")

    @Test
    fun `focus time is summed only over the selected period`() =
        runTest {
            val useCase = useCase(
                sessions = listOf(
                    session(minutes = 60, endedAt = today),
                    session(minutes = 30, endedAt = today.minusDays(1)),
                    // Outside the week: last week's Monday
                    session(minutes = 45, endedAt = today.minusDays(9)),
                ),
            )

            useCase(StatisticsPeriod.WEEK).test {
                val summary = awaitItem()
                assertEquals(90.minutes, summary.totalFocus)
            }
        }

    @Test
    fun `breaks are excluded from focused time`() =
        runTest {
            val useCase = useCase(
                sessions = listOf(
                    session(minutes = 25, endedAt = today),
                    session(minutes = 5, endedAt = today, kind = FocusKind.SHORT_BREAK),
                    session(minutes = 15, endedAt = today, kind = FocusKind.LONG_BREAK),
                ),
            )

            useCase(StatisticsPeriod.WEEK).test {
                assertEquals(25.minutes, awaitItem().totalFocus)
            }
        }

    @Test
    fun `the chart has one point per day of the period, including empty ones`() =
        runTest {
            val useCase = useCase(sessions = listOf(session(minutes = 20, endedAt = today)))

            useCase(StatisticsPeriod.WEEK).test {
                val summary = awaitItem()
                assertEquals(DAYS_IN_WEEK, summary.dailyFocus.size)
                assertEquals(20.minutes, summary.dailyFocus.first { it.date == today }.duration)
                assertEquals(ZERO, summary.dailyFocus.first { it.date == today.minusDays(1) }.duration)
            }
        }

    @Test
    fun `the week starts on the day chosen in settings`() =
        runTest {
            val useCase = useCase(settings = AppSettings(startOfWeek = DayOfWeek.SUNDAY))

            useCase(StatisticsPeriod.WEEK).test {
                // 30.09.2026 is Wednesday, the previous Sunday is the 27th
                assertEquals(LocalDate(2026, 9, 27), awaitItem().range.start)
            }
        }

    @Test
    fun `focus is split by category with shares that add up to one`() =
        runTest {
            val useCase = useCase(
                categories = listOf(workCategory, learningCategory),
                sessions = listOf(
                    session(minutes = 60, endedAt = today, categoryId = workCategory.id),
                    session(minutes = 20, endedAt = today, categoryId = learningCategory.id),
                ),
            )

            useCase(StatisticsPeriod.WEEK).test {
                val categories = awaitItem().categories
                assertEquals(listOf("Work", "Learning"), categories.map { it.categoryName })
                assertEquals(0.75f, categories.first().share, TOLERANCE)
                assertEquals(1f, categories.sumOf { it.share.toDouble() }.toFloat(), TOLERANCE)
            }
        }

    @Test
    fun `sessions without a category are reported separately instead of being dropped`() =
        runTest {
            val useCase = useCase(
                categories = listOf(workCategory),
                sessions = listOf(
                    session(minutes = 30, endedAt = today, categoryId = workCategory.id),
                    session(minutes = 30, endedAt = today, categoryId = null),
                ),
            )

            useCase(StatisticsPeriod.WEEK).test {
                val categories = awaitItem().categories
                assertEquals(2, categories.size)
                assertEquals(60.minutes, categories.fold(ZERO) { acc, it -> acc + it.duration })
            }
        }

    @Test
    fun `task completion counts only tasks of the period`() =
        runTest {
            val useCase = useCase(
                tasks = listOf(
                    task(dueDate = today, status = TaskStatus.COMPLETED),
                    task(dueDate = today, status = TaskStatus.ACTIVE),
                    task(dueDate = today.minusDays(9), status = TaskStatus.COMPLETED),
                    // Archived tasks are excluded from the completion rate
                    task(dueDate = today, status = TaskStatus.ARCHIVED),
                ),
            )

            useCase(StatisticsPeriod.WEEK).test {
                val summary = awaitItem()
                assertEquals(1, summary.completedTasks)
                assertEquals(2, summary.totalTasks)
                assertEquals(0.5f, summary.completionRate, TOLERANCE)
            }
        }

    @Test
    fun `habit consistency counts scheduled days of the period`() =
        runTest {
            val monday = today.minusDays(2)
            val habit = Habit(
                id = "habit",
                name = "Workout",
                icon = "workout",
                schedule = HabitSchedule.SelectedDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)),
                createdAt = Instant.fromEpochMilliseconds(0),
            )
            val useCase = useCase(
                habits = listOf(habit),
                completions = listOf(HabitCompletion(habitId = habit.id, date = monday, completedAt = instant(monday))),
            )

            useCase(StatisticsPeriod.WEEK).test {
                val consistency = awaitItem().habits.single()
                assertEquals(2, consistency.scheduledDays)
                assertEquals(1, consistency.completedDays)
                assertEquals(0.5f, consistency.rate, TOLERANCE)
            }
        }

    @Test
    fun `the trend compares the period with the previous one`() =
        runTest {
            val useCase = useCase(
                sessions = listOf(
                    session(minutes = 120, endedAt = today),
                    // Last week: Tuesday
                    session(minutes = 60, endedAt = today.minusDays(8)),
                ),
            )

            useCase(StatisticsPeriod.WEEK).test {
                val summary = awaitItem()
                assertEquals(60.minutes, summary.previousTotalFocus)
                assertEquals(1f, summary.focusTrend!!, TOLERANCE)
            }
        }

    @Test
    fun `the trend is absent when there is nothing to compare with`() =
        runTest {
            val useCase = useCase(sessions = listOf(session(minutes = 30, endedAt = today)))

            useCase(StatisticsPeriod.WEEK).test {
                assertEquals(null, awaitItem().focusTrend)
            }
        }

    @Test
    fun `an empty period produces zeroes rather than an error`() =
        runTest {
            val useCase = useCase()

            useCase(StatisticsPeriod.WEEK).test {
                val summary = awaitItem()
                assertEquals(ZERO, summary.totalFocus)
                assertEquals(0f, summary.completionRate, TOLERANCE)
                assertEquals(DAYS_IN_WEEK, summary.dailyFocus.size)
            }
        }

    private fun useCase(
        settings: AppSettings = AppSettings(),
        sessions: List<FocusSession> = emptyList(),
        tasks: List<Task> = emptyList(),
        habits: List<Habit> = emptyList(),
        completions: List<HabitCompletion> = emptyList(),
        categories: List<Category> = emptyList(),
    ) = ObserveStatisticsUseCase(
        settingsRepository = FakeSettingsRepository(settings),
        focusRepository = FakeFocusRepository(sessions),
        taskRepository = FakeTaskRepository(tasks),
        habitRepository = FakeHabitRepository(habits, completions),
        categoryRepository = FakeCategoryRepository(categories),
        timeProvider = timeProvider,
    )

    private fun session(
        minutes: Int,
        endedAt: LocalDate,
        kind: FocusKind = FocusKind.FOCUS,
        categoryId: String? = null,
    ): FocusSession {
        val duration: Duration = minutes.minutes
        return FocusSession(
            categoryId = categoryId,
            kind = kind,
            plannedDuration = duration,
            actualDuration = duration,
            startedAt = instant(endedAt),
            endedAt = instant(endedAt),
            completed = true,
        )
    }

    private fun task(
        dueDate: LocalDate,
        status: TaskStatus,
    ) = Task(
        title = "Task",
        dueDate = dueDate,
        status = status,
        createdAt = instant(dueDate),
    )

    private fun instant(date: LocalDate): Instant =
        Instant.fromEpochMilliseconds(date.toEpochDays().toLong() * FakeTimeProvider.MILLIS_IN_DAY)

    private fun LocalDate.minusDays(days: Int) = minus(DatePeriod(days = days))

    private companion object {
        const val TOLERANCE = 0.0001f
        const val DAYS_IN_WEEK = 7
    }
}