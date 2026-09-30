package ru.alexey.flowapp.core.model

/**
 * User selectable accent palette
 *
 * @param key stable key for DB and JSON export
 */
enum class AccentColor(
    val key: String,
) {
    PURPLE("purple"),
    VIOLET("violet"),
    BLUE("blue"),
    TEAL("teal"),
    GREEN("green"),
    ORANGE("orange"),
    PINK("pink"),
    ;

    companion object {
        val Default: AccentColor = PURPLE

        /** Unknown values fall back to [Default]. */
        fun parse(key: String?): AccentColor = entries.firstOrNull { it.key == key } ?: Default
    }
}