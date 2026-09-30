package ru.alexey.flowapp.core.model

/**
 * User category for tasks and habits
 *
 * @param icon key from [FlowIconKey]
 */
data class Category(
    val id: String = newId(),
    val name: String,
    val color: AccentColor,
    val icon: String,
    val sortOrder: Int = 0,
)