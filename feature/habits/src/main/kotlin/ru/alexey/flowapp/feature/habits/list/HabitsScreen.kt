package ru.alexey.flowapp.feature.habits.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.alexey.flowapp.core.designsystem.component.FlowCard
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.ui.component.EmptyState
import ru.alexey.flowapp.core.ui.component.FlowTopBar
import ru.alexey.flowapp.core.ui.component.HabitRow
import ru.alexey.flowapp.core.ui.component.SectionHeader
import ru.alexey.flowapp.feature.habits.R

/** Habits screen */
@Composable
fun HabitsScreen(
    state: HabitsUiState,
    onAction: (HabitsUiAction) -> Unit,
    onOpenHabit: (String) -> Unit,
    onCreateHabit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    val spacers = FlowTheme.spacers

    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
        topBar = { FlowTopBar(title = stringResource(R.string.habits_title), onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateHabit,
                containerColor = colors.primary,
                contentColor = colors.textOnAccent,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.habits_create_action),
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + spacers.x8,
                bottom = padding.calculateBottomPadding() + spacers.x24,
                start = spacers.x16,
                end = spacers.x16,
            ),
            verticalArrangement = Arrangement.spacedBy(spacers.x12),
        ) {
            if (state.isEmpty && !state.isLoading) {
                item(key = "empty") {
                    FlowCard {
                        EmptyState(
                            title = stringResource(R.string.habits_empty_title),
                            subtitle = stringResource(R.string.habits_empty_subtitle),
                        )
                    }
                }
            }

            habitSection(
                titleRes = R.string.habits_section_today,
                habits = state.todayHabits,
                onAction = onAction,
                onOpenHabit = onOpenHabit,
            )
            habitSection(
                titleRes = R.string.habits_section_other,
                habits = state.otherHabits,
                onAction = onAction,
                onOpenHabit = onOpenHabit,
            )
            habitSection(
                titleRes = R.string.habits_section_archived,
                habits = state.archivedHabits,
                onAction = onAction,
                onOpenHabit = onOpenHabit,
            )
        }
    }
}

private fun LazyListScope.habitSection(
    @androidx.annotation.StringRes titleRes: Int,
    habits: List<HabitListItemUi>,
    onAction: (HabitsUiAction) -> Unit,
    onOpenHabit: (String) -> Unit,
) {
    if (habits.isEmpty()) return

    item(key = "header_$titleRes") {
        SectionHeader(title = stringResource(titleRes), modifier = Modifier.fillMaxWidth())
    }
    items(items = habits, key = { it.id }) { habit ->
        FlowCard(contentPadding = FlowTheme.spacers.x12) {
            HabitListRow(habit = habit, onAction = onAction, onOpenHabit = onOpenHabit)
        }
    }
}

@Composable
private fun HabitListRow(
    habit: HabitListItemUi,
    onAction: (HabitsUiAction) -> Unit,
    onOpenHabit: (String) -> Unit,
) {
    val colors = FlowTheme.colors
    var menuVisible by remember { mutableStateOf(false) }

    HabitRow(
        name = habit.name,
        iconKey = habit.icon,
        accent = habit.accent,
        subtitle = habit.subtitle,
        completed = habit.completed,
        onToggle = { onAction(HabitsUiAction.ToggleHabit(habit.id, it)) },
        onClick = { onOpenHabit(habit.id) },
        trailing = {
            Box {
                IconButton(onClick = { menuVisible = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.habits_more_actions),
                        tint = colors.iconSecondary,
                    )
                }
                DropdownMenu(
                    expanded = menuVisible,
                    onDismissRequest = { menuVisible = false },
                    containerColor = colors.layer2,
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = if (habit.archived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                                contentDescription = null,
                                tint = colors.iconSecondary,
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    if (habit.archived) R.string.habits_unarchive else R.string.habits_archive,
                                ),
                                style = FlowTheme.typography.body2,
                                color = colors.textMain,
                            )
                        },
                        onClick = {
                            onAction(HabitsUiAction.SetArchived(habit.id, !habit.archived))
                            menuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.habits_delete),
                                style = FlowTheme.typography.body2,
                                color = colors.error,
                            )
                        },
                        onClick = {
                            onAction(HabitsUiAction.DeleteHabit(habit.id))
                            menuVisible = false
                        },
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun HabitsScreenPreview() {
    FlowTheme {
        HabitsScreen(
            state = HabitsUiState(
                isLoading = false,
                todayHabits = listOf(
                    HabitListItemUi("1", "Workout", "workout", AccentColor.GREEN, "0/1", false, true, false),
                    HabitListItemUi("2", "Read", "read", AccentColor.BLUE, "1/1 · 4 day streak", true, true, false),
                ),
            ),
            onAction = {},
            onOpenHabit = {},
            onCreateHabit = {},
            onBack = {},
        )
    }
}