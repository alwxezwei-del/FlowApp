package ru.alexey.flowapp.feature.focus.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.ui.component.EmptyState
import ru.alexey.flowapp.feature.focus.FocusTaskUi
import ru.alexey.flowapp.feature.focus.R

/**
 * Task picker for a session
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskPickerSheet(
    tasks: List<FocusTaskUi>,
    selectedTaskId: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = FlowTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.layer1,
    ) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = FlowTheme.spacers.x16),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = FlowTheme.spacers.x24,
            ),
        ) {
            item(key = "title") {
                Text(
                    text = stringResource(R.string.focus_pick_task),
                    style = FlowTheme.typography.title2,
                    color = colors.textMain,
                    modifier = Modifier.padding(vertical = FlowTheme.spacers.x12),
                )
            }

            item(key = "none") {
                PickerRow(
                    title = stringResource(R.string.focus_no_task),
                    subtitle = null,
                    selected = selectedTaskId == null,
                    onClick = { onSelect(null) },
                )
            }

            if (tasks.isEmpty()) {
                item(key = "empty") {
                    EmptyState(title = stringResource(R.string.focus_tasks_empty))
                }
            }

            items(items = tasks, key = { it.id }) { task ->
                PickerRow(
                    title = task.title,
                    subtitle = task.subtitle,
                    selected = task.id == selectedTaskId,
                    onClick = { onSelect(task.id) },
                )
            }
        }
    }
}

@Composable
private fun PickerRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = FlowTheme.colors
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = FlowTheme.spacers.x12),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x12),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = FlowTheme.typography.body1, color = colors.textMain)
            if (subtitle != null) {
                Text(text = subtitle, style = FlowTheme.typography.caption, color = colors.textSecondary)
            }
        }
        if (selected) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = colors.primary)
        }
    }
}