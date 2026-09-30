package ru.alexey.flowapp.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import ru.alexey.flowapp.core.ui.navigation.FlowRoute
import ru.alexey.flowapp.core.ui.navigation.TopLevelDestination
import kotlin.reflect.KClass

/**
 * Navigates to a bottom-bar tab
 */
fun NavHostController.navigateToTopLevel(route: FlowRoute.TopLevel) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Checks the whole destination hierarchy so the tab stays highlighted on nested screens */
fun TopLevelDestination.isSelected(destination: NavDestination?): Boolean =
    destination?.hierarchy?.any { it.hasRoute(route.routeClass()) } == true

private fun FlowRoute.TopLevel.routeClass(): KClass<*> =
    when (this) {
        FlowRoute.Home -> FlowRoute.Home::class
        FlowRoute.Tasks -> FlowRoute.Tasks::class
        is FlowRoute.Focus -> FlowRoute.Focus::class
        FlowRoute.Statistics -> FlowRoute.Statistics::class
    }

/** Whether the destination is a bottom-bar tab. The bar is hidden on detail screens */
fun isTopLevel(destination: NavDestination?): Boolean = TopLevelDestination.entries.any { it.matchesExactly(destination) }

private fun TopLevelDestination.matchesExactly(destination: NavDestination?): Boolean = destination?.hasRoute(route.routeClass()) == true

private val NavDestination.hierarchy: Sequence<NavDestination>
    get() = generateSequence(this) { it.parent }