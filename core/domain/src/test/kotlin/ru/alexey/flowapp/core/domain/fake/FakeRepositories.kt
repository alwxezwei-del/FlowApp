package ru.alexey.flowapp.core.domain.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.CategoryRepository
import ru.alexey.flowapp.core.domain.repository.FocusRepository
import ru.alexey.flowapp.core.domain.repository.HabitRepository
import ru.alexey.flowapp.core.domain.repository.SettingsRepository
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.AppSettings
import ru.alexey.flowapp.core.model.Category
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.FocusSession
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitCompletion
import ru.alexey.flowapp.core.model.HabitDayProgress
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import ru.alexey.flowapp.core.model.ThemeMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Instant

// In-memory fake repositories for use case tests, exposing the same flows as the real ones

/** @param today date seen by the test instead of the system one */
class FakeTimeProvider(
    var today: LocalDate,
) : TimeProvider {
    override fun now(): Instant = Instant.fromEpochMilliseconds(today.toEpochDays().toLong() * MILLIS_IN_DAY)

    override fun timeZone(): TimeZone = TimeZone.UTC

    override fun today(): LocalDate = today

    companion object {
        const val MILLIS_IN_DAY = 86_400_000L
    }
}

class FakeSettingsRepository(
    settings: AppSettings = AppSettings(),
) : SettingsRepository {
    val state = MutableStateFlow(settings)

    override fun observeSettings(): Flow<AppSettings> = state

    override fun currentThemeMode(): ThemeMode = state.value.themeMode

    override suspend fun updateSettings(settings: AppSettings) {
        state.value = settings
    }
}

class FakeCategoryRepository(
    categories: List<Category> = emptyList(),
) : CategoryRepository {
    val state = MutableStateFlow(categories)

    override fun observeCategories(): Flow<List<Category>> = state

    override suspend fun getCategory(id: String): Category? = state.value.firstOrNull { it.id == id }

    override suspend fun createCategory(category: Category) {
        state.value += category
    }

    override suspend fun updateCategory(category: Category) {
        state.value = state.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun deleteCategory(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}

class FakeTaskRepository(
    tasks: List<Task> = emptyList(),
) : TaskRepository {
    val state = MutableStateFlow(tasks)

    override fun observeTasks(): Flow<List<Task>> = state

    override fun observeTasksForDate(date: LocalDate): Flow<List<Task>> = state.map { tasks -> tasks.filter { it.dueDate == date } }

    override fun observeTasksInRange(range: DateRange): Flow<List<Task>> =
        state.map { tasks -> tasks.filter { task -> task.dueDate?.let { it in range } == true } }

    override fun observeTask(id: String): Flow<Task?> = state.map { tasks -> tasks.firstOrNull { it.id == id } }

    override suspend fun getTask(id: String): Task? = state.value.firstOrNull { it.id == id }

    override suspend fun createTask(task: Task) {
        state.value += task
    }

    override suspend fun updateTask(task: Task) {
        state.value = state.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun setStatus(
        id: String,
        status: TaskStatus,
    ) {
        state.value = state.value.map { if (it.id == id) it.copy(status = status) else it }
    }

    override suspend fun reschedule(
        id: String,
        date: LocalDate?,
    ) {
        state.value = state.value.map { if (it.id == id) it.copy(dueDate = date) else it }
    }

    override suspend fun addFocusedTime(
        id: String,
        duration: Duration,
    ) {
        state.value = state.value.map { if (it.id == id) it.copy(focusedTime = it.focusedTime + duration) else it }
    }

    override suspend fun deleteTask(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}

class FakeHabitRepository(
    habits: List<Habit> = emptyList(),
    completions: List<HabitCompletion> = emptyList(),
) : HabitRepository {
    val habitsState = MutableStateFlow(habits)
    val completionsState = MutableStateFlow(completions)

    override fun observeHabits(): Flow<List<Habit>> = habitsState.map { list -> list.filterNot { it.archived } }

    override fun observeAllHabits(): Flow<List<Habit>> = habitsState

    override fun observeHabit(id: String): Flow<Habit?> = habitsState.map { list -> list.firstOrNull { it.id == id } }

    override fun observeProgressForDate(date: LocalDate): Flow<List<HabitDayProgress>> =
        habitsState.map { list ->
            list
                .filter { !it.archived && it.schedule.isScheduledOn(date) }
                .map { habit ->
                    HabitDayProgress(
                        habit = habit,
                        date = date,
                        completedCount = completionsState.value.count { it.habitId == habit.id && it.date == date },
                        scheduled = true,
                    )
                }
        }

    override fun observeCompletions(range: DateRange): Flow<List<HabitCompletion>> =
        completionsState.map { list -> list.filter { it.date in range } }

    override fun observeCompletions(habitId: String): Flow<List<HabitCompletion>> =
        completionsState.map { list -> list.filter { it.habitId == habitId } }

    override fun observeAllCompletions(): Flow<List<HabitCompletion>> = completionsState

    override suspend fun createHabit(habit: Habit) {
        habitsState.value += habit
    }

    override suspend fun updateHabit(habit: Habit) {
        habitsState.value = habitsState.value.map { if (it.id == habit.id) habit else it }
    }

    override suspend fun complete(
        habitId: String,
        date: LocalDate,
    ) {
        completionsState.value += HabitCompletion(
            habitId = habitId,
            date = date,
            completedAt = Instant.fromEpochMilliseconds(date.toEpochDays().toLong() * FakeTimeProvider.MILLIS_IN_DAY),
        )
    }

    override suspend fun undoComplete(
        habitId: String,
        date: LocalDate,
    ) {
        val last = completionsState.value.lastOrNull { it.habitId == habitId && it.date == date } ?: return
        completionsState.value = completionsState.value - last
    }

    override suspend fun setArchived(
        id: String,
        archived: Boolean,
    ) {
        habitsState.value = habitsState.value.map { if (it.id == id) it.copy(archived = archived) else it }
    }

    override suspend fun deleteHabit(id: String) {
        habitsState.value = habitsState.value.filterNot { it.id == id }
        completionsState.value = completionsState.value.filterNot { it.habitId == id }
    }
}

class FakeFocusRepository(
    sessions: List<FocusSession> = emptyList(),
    private val timeZone: TimeZone = TimeZone.UTC,
) : FocusRepository {
    val state = MutableStateFlow(sessions)

    override fun observeSessions(range: DateRange): Flow<List<FocusSession>> =
        state.map { list -> list.filter { it.endedAt.toLocalDateTime(timeZone).date in range } }

    override fun observeRecentSessions(limit: Int): Flow<List<FocusSession>> =
        state.map { list -> list.sortedByDescending { it.endedAt }.take(limit) }

    override fun observeTotalFocus(range: DateRange): Flow<Duration> =
        observeSessions(range).map { list ->
            list
                .filter { it.kind == FocusKind.FOCUS }
                .fold(ZERO) { acc, session -> acc + session.actualDuration }
        }

    override suspend fun saveSession(session: FocusSession) {
        state.value += session
    }

    override suspend fun deleteSession(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}