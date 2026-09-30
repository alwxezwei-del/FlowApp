package ru.alexey.flowapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import ru.alexey.flowapp.core.ui.navigation.FlowRoute
import ru.alexey.flowapp.feature.focus.navigation.focusScreen
import ru.alexey.flowapp.feature.habits.navigation.habitDetailsScreen
import ru.alexey.flowapp.feature.habits.navigation.habitEditorScreen
import ru.alexey.flowapp.feature.habits.navigation.habitsScreen
import ru.alexey.flowapp.feature.history.navigation.historyScreen
import ru.alexey.flowapp.feature.home.navigation.homeScreen
import ru.alexey.flowapp.feature.settings.navigation.settingsScreen
import ru.alexey.flowapp.feature.statistics.navigation.statisticsScreen
import ru.alexey.flowapp.feature.tasks.navigation.taskEditorScreen
import ru.alexey.flowapp.feature.tasks.navigation.tasksScreen

/**
 * App navigation graph
 */
@Composable
fun FlowNavHost(
    navController: NavHostController,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    val onBack: () -> Unit = { navController.navigateUp() }

    NavHost(
        navController = navController,
        startDestination = FlowRoute.Home,
        modifier = modifier,
    ) {
        homeScreen(
            onOpenTask = { navController.navigate(FlowRoute.TaskEditor(it)) },
            onOpenHabit = { navController.navigate(FlowRoute.HabitDetails(it)) },
            onOpenHabits = { navController.navigate(FlowRoute.Habits) },
            onOpenHistory = { navController.navigate(FlowRoute.History) },
            onOpenSettings = { navController.navigate(FlowRoute.Settings) },
        )

        tasksScreen(
            onOpenTask = { navController.navigate(FlowRoute.TaskEditor(it)) },
            onCreateTask = { navController.navigate(FlowRoute.TaskEditor()) },
            onStartFocus = { navController.navigateToTopLevel(FlowRoute.Focus(it)) },
        )
        taskEditorScreen(onBack = onBack)

        focusScreen()

        statisticsScreen()

        habitsScreen(
            onOpenHabit = { navController.navigate(FlowRoute.HabitDetails(it)) },
            onCreateHabit = { navController.navigate(FlowRoute.HabitEditor()) },
            onBack = onBack,
        )
        habitDetailsScreen(
            onEditHabit = { navController.navigate(FlowRoute.HabitEditor(it)) },
            onBack = onBack,
        )
        habitEditorScreen(onBack = onBack)

        historyScreen(onBack = onBack)

        settingsScreen(appVersion = appVersion, onBack = onBack)
    }
}