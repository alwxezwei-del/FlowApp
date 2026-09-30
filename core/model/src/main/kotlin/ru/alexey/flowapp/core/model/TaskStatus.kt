package ru.alexey.flowapp.core.model

/**
 * Task status
 *
 * @param key stable storage key
 */
enum class TaskStatus(
    val key: String,
) {
    ACTIVE("active"),

    COMPLETED("completed"),

    ARCHIVED("archived"),
    ;

    companion object {
        fun parse(key: String?): TaskStatus = entries.firstOrNull { it.key == key } ?: ACTIVE
    }
}