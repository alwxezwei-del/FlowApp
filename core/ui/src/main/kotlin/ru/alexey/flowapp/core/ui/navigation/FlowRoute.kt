package ru.alexey.flowapp.core.ui.navigation

import kotlinx.serialization.Serializable

sealed interface FlowRoute {
    sealed interface TopLevel : FlowRoute

    @Serializable
    data object Home : TopLevel

    @Serializable
    data object Tasks : TopLevel

    @Serializable
    data class Focus(
        val taskId: String? = null,
    ) : TopLevel

    @Serializable
    data object Statistics : TopLevel

    /**
     * @param taskId null = new task
     */
    @Serializable
    data class TaskEditor(
        val taskId: String? = null,
    ) : FlowRoute

    /** @param habitId null = new habit */
    @Serializable
    data class HabitEditor(
        val habitId: String? = null,
    ) : FlowRoute

    @Serializable
    data class HabitDetails(
        val habitId: String,
    ) : FlowRoute

    @Serializable
    data object Habits : FlowRoute

    @Serializable
    data object History : FlowRoute

    @Serializable
    data object Settings : FlowRoute

    @Serializable
    data object Categories : FlowRoute
}