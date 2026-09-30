package ru.alexey.flowapp.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.designsystem.component.FlowCard
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.ui.component.EmptyState
import ru.alexey.flowapp.core.ui.component.HabitRow
import ru.alexey.flowapp.core.ui.component.SectionHeader
import ru.alexey.flowapp.core.ui.component.TaskRow
import ru.alexey.flowapp.core.ui.format.formatFullDate
import ru.alexey.flowapp.feature.home.views.FocusSummaryCard
import ru.alexey.flowapp.feature.home.views.WeekStrip

/**
 * Home screen
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    onOpenTask: (String) -> Unit,
    onOpenHabit: (String) -> Unit,
    onOpenHabits: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
    ) { padding ->
        when (state) {
            HomeUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.primary)
            }

            is HomeUiState.Content -> HomeContent(
                state = state,
                onAction = onAction,
                onOpenTask = onOpenTask,
                onOpenHabit = onOpenHabit,
                onOpenHabits = onOpenHabits,
                onOpenHistory = onOpenHistory,
                onOpenSettings = onOpenSettings,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Content,
    onAction: (HomeUiAction) -> Unit,
    onOpenTask: (String) -> Unit,
    onOpenHabit: (String) -> Unit,
    onOpenHabits: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    contentPadding: PaddingValues,
) {
    val spacers = FlowTheme.spacers

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + spacers.x8,
            bottom = contentPadding.calculateBottomPadding() + spacers.x24,
            start = spacers.x16,
            end = spacers.x16,
        ),
        verticalArrangement = Arrangement.spacedBy(spacers.x16),
    ) {
        item(key = "header") {
            HomeHeader(
                greeting = state.greeting,
                date = state.date,
                onOpenHistory = onOpenHistory,
                onOpenSettings = onOpenSettings,
            )
        }

        item(key = "week") {
            WeekStrip(
                days = state.weekDays,
                selected = state.date,
                today = state.today,
                onSelect = { onAction(HomeUiAction.SelectDate(it)) },
            )
        }

        item(key = "focus") {
            FocusSummaryCard(
                total = state.focusTotal,
                trend = state.focusTrendPercent,
                taskProgress = stringResource(
                    R.string.today_tasks_progress,
                    state.completedTasks,
                    state.totalTasks,
                ),
            )
        }

        item(key = "tasks_header") {
            SectionHeader(
                title = stringResource(R.string.today_tasks_title),
                trailing = "${state.completedTasks}/${state.totalTasks}".takeIf { state.totalTasks > 0 },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.tasks.isEmpty()) {
            item(key = "tasks_empty") {
                FlowCard {
                    EmptyState(
                        title = stringResource(R.string.today_tasks_empty_title),
                        subtitle = stringResource(R.string.today_tasks_empty_subtitle),
                    )
                }
            }
        } else {
            item(key = "tasks") {
                FlowCard(contentPadding = FlowTheme.spacers.x12) {
                    state.tasks.forEach { task ->
                        TaskRow(
                            title = task.title,
                            subtitle = task.subtitle,
                            completed = task.completed,
                            accent = task.accent,
                            progress = task.progress,
                            onToggle = { onAction(HomeUiAction.ToggleTask(task.id, it)) },
                            onClick = { onOpenTask(task.id) },
                        )
                    }
                }
            }
        }

        item(key = "habits_header") {
            SectionHeader(
                title = stringResource(R.string.today_habits_title),
                trailing = stringResource(R.string.today_habits_manage),
                onTrailingClick = onOpenHabits,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.habits.isEmpty()) {
            item(key = "habits_empty") {
                FlowCard {
                    EmptyState(
                        title = stringResource(R.string.today_habits_empty_title),
                        subtitle = stringResource(R.string.today_habits_empty_subtitle),
                    )
                }
            }
        } else {
            items(items = state.habits, key = { it.id }) { habit ->
                FlowCard(contentPadding = FlowTheme.spacers.x12) {
                    HabitRow(
                        name = habit.name,
                        iconKey = habit.icon,
                        accent = habit.accent,
                        subtitle = habit.subtitle,
                        completed = habit.completed,
                        onToggle = { onAction(HomeUiAction.ToggleHabit(habit.id, it)) },
                        onClick = { onOpenHabit(habit.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: Greeting,
    date: LocalDate,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = FlowTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x2),
        ) {
            Text(
                text = stringResource(greeting.titleRes()),
                style = FlowTheme.typography.title1,
                color = colors.textMain,
            )
            Text(
                text = date.formatFullDate(),
                style = FlowTheme.typography.body2,
                color = colors.textSecondary,
            )
        }
        IconButton(onClick = onOpenHistory) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = stringResource(R.string.today_open_history),
                tint = colors.iconSecondary,
            )
        }
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.today_open_settings),
                tint = colors.iconSecondary,
            )
        }
    }
}

private fun Greeting.titleRes(): Int =
    when (this) {
        Greeting.MORNING -> R.string.today_greeting_morning
        Greeting.AFTERNOON -> R.string.today_greeting_afternoon
        Greeting.EVENING -> R.string.today_greeting_evening
    }

@Preview
@Composable
private fun HomeScreenPreview() {
    FlowTheme {
        HomeScreen(
            state = HomeUiState.Content(
                date = LocalDate(2026, 9, 30),
                today = LocalDate(2026, 9, 30),
                weekDays = (28..30).map { LocalDate(2026, 9, it) } + (1..4).map { LocalDate(2026, 10, it) },
                greeting = Greeting.AFTERNOON,
                focusTotal = "1h 40m",
                focusTrendPercent = 0.18f,
                tasks = listOf(
                    TaskUi("1", "Finish", "Work · 45m", false, AccentColor.PURPLE),
                    TaskUi("2", "Review PR", "Work · 20m", true, AccentColor.PURPLE),
                ),
                completedTasks = 3,
                totalTasks = 5,
                habits = listOf(
                    HabitUi("h1", "Walk", "walk", AccentColor.GREEN, "1/1 · 12 day streak", true),
                    HabitUi("h2", "Read", "read", AccentColor.BLUE, "0/1 · 4 day streak", false),
                ),
            ),
            onAction = {},
            onOpenTask = {},
            onOpenHabit = {},
            onOpenHabits = {},
            onOpenHistory = {},
            onOpenSettings = {},
        )
    }
}