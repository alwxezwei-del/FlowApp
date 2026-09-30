package ru.alexey.flowapp.core.model

/**
 * Task priority
 *
 * @param weight sort weight, higher goes first
 */
enum class Priority(
    val key: String,
    val weight: Int,
) {
    LOW("low", 0),
    MEDIUM("medium", 1),
    HIGH("high", 2),
    ;

    companion object {
        val Default: Priority = MEDIUM

        fun parse(key: String?): Priority = entries.firstOrNull { it.key == key } ?: Default
    }
}