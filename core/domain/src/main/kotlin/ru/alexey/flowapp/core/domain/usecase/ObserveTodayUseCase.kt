package ru.alexey.flowapp.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.koin.core.annotation.Factory
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.CategoryRepository
import ru.alexey.flowapp.core.domain.repository.FocusRepository
import ru.alexey.flowapp.core.domain.repository.HabitRepository
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.Category
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.HabitDayProgress
import ru.alexey.flowapp.core.model.Task
import ru.alexey.flowapp.core.model.TaskStatus
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

/**
 * Today screen data for a single day
 *
 * @param focusYesterday used for the comparison line
 */
data class TodaySnapshot(
    val date: LocalDate,
    val tasks: List<Task> = emptyList(),
    val habits: List<HabitDayProgress> = emptyList(),
    val categories: List<Category> = emptyList(),
    val focusToday: Duration = ZERO,
    val focusYesterday: Duration = ZERO,
) {
    val completedTasks: Int get() = tasks.count { it.status == TaskStatus.COMPLETED }

    /** Non archived tasks count */
    val totalTasks: Int get() = tasks.count { it.status != TaskStatus.ARCHIVED }

    /** Focus time change vs yesterday as a fraction */
    val focusTrend: Float?
        get() {
            val yesterday = focusYesterday.inWholeSeconds
            if (yesterday <= 0L) return null
            return (focusToday.inWholeSeconds - yesterday).toFloat() / yesterday
        }
}

/** Combines four repositories into a Today snapshot */
@Factory
class ObserveTodayUseCase(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val focusRepository: FocusRepository,
    private val categoryRepository: CategoryRepository,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(date: LocalDate = timeProvider.today()): Flow<TodaySnapshot> {
        val yesterday = date.minus(DatePeriod(days = 1))
        return combine(
            taskRepository.observeTasksForDate(date),
            habitRepository.observeProgressForDate(date),
            categoryRepository.observeCategories(),
            focusRepository.observeTotalFocus(DateRange(date, date)),
            focusRepository.observeTotalFocus(DateRange(yesterday, yesterday)),
        ) { tasks, habits, categories, focusToday, focusYesterday ->
            TodaySnapshot(
                date = date,
                tasks = tasks,
                habits = habits,
                categories = categories,
                focusToday = focusToday,
                focusYesterday = focusYesterday,
            )
        }
    }
}