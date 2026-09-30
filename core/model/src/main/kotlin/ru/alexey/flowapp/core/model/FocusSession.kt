package ru.alexey.flowapp.core.model

import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Timer interval kind
 *
 * @param key stable storage key
 */
enum class FocusKind(
    val key: String,
) {
    /** Work interval */
    FOCUS("focus"),

    SHORT_BREAK("short_break"),

    LONG_BREAK("long_break"),
    ;

    companion object {
        fun parse(key: String?): FocusKind = entries.firstOrNull { it.key == key } ?: FOCUS
    }
}

/**
 * Completed or stopped timer session. Saved on stop; stats use [actualDuration].
 *
 * @param taskId null for a free session
 * @param categoryId copied at session time so stats survive task changes
 * @param actualDuration run time excluding pauses
 * @param completed timer reached zero
 */
data class FocusSession(
    val id: String = newId(),
    val taskId: String? = null,
    val categoryId: String? = null,
    val kind: FocusKind = FocusKind.FOCUS,
    val plannedDuration: Duration,
    val actualDuration: Duration,
    val startedAt: Instant,
    val endedAt: Instant,
    val completed: Boolean,
)