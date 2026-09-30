package ru.alexey.flowapp.feature.tasks.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Sort
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.alexey.flowapp.core.designsystem.component.FlowCard
import ru.alexey.flowapp.core.designsystem.component.FlowSegmentedControl
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.Priority
import ru.alexey.flowapp.core.ui.component.EmptyState
import ru.alexey.flowapp.core.ui.component.FlowTopBar
import ru.alexey.flowapp.feature.tasks.R
import ru.alexey.flowapp.feature.tasks.list.views.TaskListRow

/** Task list with filters, sorting and creation */
@Composable
fun TasksScreen(
    state: TasksUiState,
    onAction: (TasksUiAction) -> Unit,
    onOpenTask: (String) -> Unit,
    onCreateTask: () -> Unit,
    onStartFocus: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    var sortMenuVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
        topBar = {
            FlowTopBar(title = stringResource(R.string.tasks_title)) {
                Box {
                    IconButton(onClick = { sortMenuVisible = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Sort,
                            contentDescription = stringResource(R.string.tasks_sort_action),
                            tint = colors.iconSecondary,
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuVisible,
                        onDismissRequest = { sortMenuVisible = false },
                        containerColor = colors.layer2,
                    ) {
                        TasksSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(sort.labelRes),
                                        style = FlowTheme.typography.body2,
                                        color = if (sort == state.sort) colors.primary else colors.textMain,
                                    )
                                },
                                onClick = {
                                    onAction(TasksUiAction.SelectSort(sort))
                                    sortMenuVisible = false
                                },
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTask,
                containerColor = colors.primary,
                contentColor = colors.textOnAccent,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.tasks_create_action),
                )
            }
        },
    ) { padding ->
        val spacers = FlowTheme.spacers
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
            item(key = "filters") {
                FlowSegmentedControl(
                    items = TasksFilter.entries.map { stringResource(it.labelRes) },
                    selectedIndex = TasksFilter.entries.indexOf(state.filter),
                    onSelect = { onAction(TasksUiAction.SelectFilter(TasksFilter.entries[it])) },
                )
            }

            if (state.tasks.isEmpty() && !state.isLoading) {
                item(key = "empty") {
                    FlowCard {
                        EmptyState(
                            title = stringResource(R.string.tasks_empty_title),
                            subtitle = stringResource(R.string.tasks_empty_subtitle),
                        )
                    }
                }
            }

            items(items = state.tasks, key = { it.id }) { task ->
                FlowCard(contentPadding = spacers.x12) {
                    TaskListRow(
                        task = task,
                        onToggle = { onAction(TasksUiAction.ToggleTask(task.id, it)) },
                        onClick = { onOpenTask(task.id) },
                        onStartFocus = { onStartFocus(task.id) },
                        onMoveToTomorrow = { onAction(TasksUiAction.MoveToTomorrow(task.id)) },
                        onDelete = { onAction(TasksUiAction.DeleteTask(task.id)) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun TasksScreenPreview() {
    FlowTheme {
        TasksScreen(
            state = TasksUiState(
                isLoading = false,
                tasks = listOf(
                    TaskListItemUi(
                        id = "1",
                        title = "Finish Compose navigation",
                        subtitle = "Work · Sep 30 · 45m",
                        completed = false,
                        priority = Priority.HIGH,
                        accent = AccentColor.PURPLE,
                        hasDueDate = true,
                    ),
                    TaskListItemUi(
                        id = "2",
                        title = "Read Android article",
                        subtitle = "Learning · 20m",
                        completed = true,
                        priority = Priority.LOW,
                        accent = AccentColor.BLUE,
                        hasDueDate = false,
                    ),
                ),
            ),
            onAction = {},
            onOpenTask = {},
            onCreateTask = {},
            onStartFocus = {},
        )
    }
}