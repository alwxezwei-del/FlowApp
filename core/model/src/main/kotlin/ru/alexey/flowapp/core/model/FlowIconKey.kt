package ru.alexey.flowapp.core.model

/**
 * Icon keys for habits and categories
 */
object FlowIconKey {
    const val WORKOUT = "workout"
    const val READ = "read"
    const val CODE = "code"
    const val MEDITATE = "meditate"
    const val HEART = "heart"
    const val STAR = "star"
    const val WATER = "water"
    const val WALK = "walk"
    const val WORK = "work"
    const val LEARNING = "learning"
    const val PERSONAL = "personal"
    const val HEALTH = "health"

    /** All keys in icon picker order */
    val All: List<String> = listOf(
        WORKOUT,
        READ,
        CODE,
        MEDITATE,
        HEART,
        STAR,
        WATER,
        WALK,
        WORK,
        LEARNING,
        PERSONAL,
        HEALTH,
    )

    val Default: String = STAR
}