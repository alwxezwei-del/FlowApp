package ru.alexey.flowapp.feature.tasks.list.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.Priority
import ru.alexey.flowapp.core.ui.component.TaskRow
import ru.alexey.flowapp.feature.tasks.R
import ru.alexey.flowapp.feature.tasks.list.TaskListItemUi

/** Task row with an overflow menu for "move to tomorrow" and delete. */
@Composable
internal fun TaskListRow(
    task: TaskListItemUi,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onStartFocus: () -> Unit,
    onMoveToTomorrow: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    var menuVisible by remember { mutableStateOf(false) }

    TaskRow(
        modifier = modifier,
        title = task.title,
        subtitle = task.subtitle,
        completed = task.completed,
        accent = task.accent,
        progress = task.progress,
        onToggle = onToggle,
        onClick = onClick,
        trailing = {
            if (!task.completed) {
                IconButton(onClick = onStartFocus) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(R.string.tasks_start_focus),
                        tint = if (task.priority == Priority.HIGH) colors.primary else colors.iconSecondary,
                    )
                }
            }
            Box {
                IconButton(onClick = { menuVisible = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.tasks_more_actions),
                        tint = colors.iconSecondary,
                    )
                }
                DropdownMenu(
                    expanded = menuVisible,
                    onDismissRequest = { menuVisible = false },
                    containerColor = colors.layer2,
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.tasks_move_to_tomorrow),
                                style = FlowTheme.typography.body2,
                                color = colors.textMain,
                            )
                        },
                        onClick = {
                            onMoveToTomorrow()
                            menuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.tasks_delete),
                                style = FlowTheme.typography.body2,
                                color = colors.error,
                            )
                        },
                        onClick = {
                            onDelete()
                            menuVisible = false
                        },
                    )
                }
            }
        },
    )
}