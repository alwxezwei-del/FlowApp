package ru.alexey.flowapp.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory
import ru.alexey.flowapp.core.domain.repository.CategoryRepository
import ru.alexey.flowapp.core.domain.repository.FocusRepository
import ru.alexey.flowapp.core.domain.repository.HabitRepository
import ru.alexey.flowapp.core.domain.repository.TaskRepository
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.DateRange
import ru.alexey.flowapp.core.model.HistoryEvent
import ru.alexey.flowapp.core.model.HistoryFilter
import ru.alexey.flowapp.core.model.TaskStatus

/**
 * History feed, built from tasks, habit completions and focus sessions sorted by time
 */
@Factory
class ObserveHistoryUseCase(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val focusRepository: FocusRepository,
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(
        range: DateRange,
        filter: HistoryFilter = HistoryFilter.Default,
    ): Flow<List<HistoryEvent>> =
        combine(
            taskRepository.observeTasksInRange(range),
            habitRepository.observeCompletions(range),
            habitRepository.observeAllHabits(),
            focusRepository.observeSessions(range),
            categoryRepository.observeCategories(),
        ) { tasks, completions, habits, sessions, categories ->
            val categoriesById = categories.associateBy { it.id }
            val habitsById = habits.associateBy { it.id }
            val tasksById = tasks.associateBy { it.id }

            buildList {
                if (filter == HistoryFilter.ALL || filter == HistoryFilter.TASKS) {
                    tasks
                        .filter { it.status == TaskStatus.COMPLETED }
                        .forEach { task ->
                            val category = task.categoryId?.let(categoriesById::get)
                            add(
                                HistoryEvent.TaskCompleted(
                                    id = task.id,
                                    timestamp = task.completedAt ?: task.createdAt,
                                    title = task.title,
                                    categoryName = category?.name,
                                    color = category?.color ?: AccentColor.Default,
                                ),
                            )
                        }
                }

                if (filter == HistoryFilter.ALL || filter == HistoryFilter.HABITS) {
                    completions.forEach { completion ->
                        val habit = habitsById[completion.habitId] ?: return@forEach
                        add(
                            HistoryEvent.HabitCompleted(
                                id = completion.id,
                                timestamp = completion.completedAt,
                                habitName = habit.name,
                                icon = habit.icon,
                                color = habit.color,
                            ),
                        )
                    }
                }

                if (filter == HistoryFilter.ALL || filter == HistoryFilter.FOCUS) {
                    sessions.forEach { session ->
                        add(
                            HistoryEvent.FocusFinished(
                                id = session.id,
                                timestamp = session.endedAt,
                                taskTitle = session.taskId?.let { tasksById[it]?.title },
                                kind = session.kind,
                                duration = session.actualDuration,
                                completed = session.completed,
                            ),
                        )
                    }
                }
            }.sortedByDescending { it.timestamp }
        }
}