package ru.alexey.flowapp.core.model

import kotlin.time.Duration
import kotlin.time.Instant

/**
 * History event type, used as a filter
 *
 * @param key stable storage key
 */
enum class HistoryFilter(
    val key: String,
) {
    ALL("all"),
    FOCUS("focus"),
    TASKS("tasks"),
    HABITS("habits"),
    ;

    companion object {
        val Default: HistoryFilter = ALL

        fun parse(key: String?): HistoryFilter = entries.firstOrNull { it.key == key } ?: Default
    }
}

/** History feed event, built from tasks, habit completions and focus sessions */
sealed interface HistoryEvent {
    /** Sort key for the feed */
    val timestamp: Instant

    /** List key */
    val id: String

    /**
     * Task completed
     *
     * @param categoryName null = no category
     */
    data class TaskCompleted(
        override val id: String,
        override val timestamp: Instant,
        val title: String,
        val categoryName: String?,
        val color: AccentColor,
    ) : HistoryEvent

    /** Habit completed */
    data class HabitCompleted(
        override val id: String,
        override val timestamp: Instant,
        val habitName: String,
        val icon: String,
        val color: AccentColor,
    ) : HistoryEvent

    /**
     * Focus session finished
     *
     * @param taskTitle null for a free session
     * @param completed timer reached zero
     */
    data class FocusFinished(
        override val id: String,
        override val timestamp: Instant,
        val taskTitle: String?,
        val kind: FocusKind,
        val duration: Duration,
        val completed: Boolean,
    ) : HistoryEvent
}