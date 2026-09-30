package ru.alexey.flowapp.feature.tasks.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import ru.alexey.flowapp.core.designsystem.component.FlowChip
import ru.alexey.flowapp.core.designsystem.component.FlowPrimaryButton
import ru.alexey.flowapp.core.designsystem.component.FlowTextButton
import ru.alexey.flowapp.core.designsystem.component.FlowTextField
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.Priority
import ru.alexey.flowapp.core.ui.component.FlowTopBar
import ru.alexey.flowapp.core.ui.format.formatShortDate
import ru.alexey.flowapp.feature.tasks.R

/** Estimate presets, same as timer presets */
private val EstimatePresets = listOf(15, 25, 45, 60)

/**
 * Create/edit task screen
 *
 * @param today used for due date presets
 */
@Composable
fun TaskEditorScreen(
    state: TaskEditorUiState,
    today: LocalDate,
    onAction: (TaskEditorUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    val spacers = FlowTheme.spacers
    var datePickerVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
        topBar = {
            FlowTopBar(
                title = stringResource(
                    if (state.isEditing) R.string.task_editor_title_edit else R.string.task_editor_title_create,
                ),
                onBack = onBack,
                actions = {
                    if (state.isEditing) {
                        FlowTextButton(
                            text = stringResource(R.string.task_editor_delete),
                            onClick = { onAction(TaskEditorUiAction.Delete) },
                            destructive = true,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacers.x16, vertical = spacers.x8),
            verticalArrangement = Arrangement.spacedBy(spacers.x20),
        ) {
            FlowTextField(
                label = stringResource(R.string.task_editor_field_title),
                value = state.title,
                onValueChange = { onAction(TaskEditorUiAction.TitleChanged(it)) },
                placeholder = stringResource(R.string.task_editor_field_title_hint),
            )

            FlowTextField(
                label = stringResource(R.string.task_editor_field_description),
                value = state.description,
                onValueChange = { onAction(TaskEditorUiAction.DescriptionChanged(it)) },
                placeholder = stringResource(R.string.task_editor_field_description_hint),
                singleLine = false,
                minLines = 3,
                keyboardType = KeyboardType.Text,
            )

            EditorSection(title = stringResource(R.string.task_editor_field_category)) {
                FlowChip(
                    text = stringResource(R.string.task_editor_category_none),
                    selected = state.categoryId == null,
                    onClick = { onAction(TaskEditorUiAction.CategorySelected(null)) },
                )
                state.categories.forEach { category ->
                    FlowChip(
                        text = category.name,
                        selected = state.categoryId == category.id,
                        onClick = { onAction(TaskEditorUiAction.CategorySelected(category.id)) },
                        accent = colors.accent(category.color),
                    )
                }
            }

            EditorSection(title = stringResource(R.string.task_editor_field_priority)) {
                Priority.entries.forEach { priority ->
                    FlowChip(
                        text = stringResource(priority.labelRes()),
                        selected = state.priority == priority,
                        onClick = { onAction(TaskEditorUiAction.PrioritySelected(priority)) },
                    )
                }
            }

            EditorSection(title = stringResource(R.string.task_editor_field_estimate)) {
                FlowChip(
                    text = stringResource(R.string.task_editor_estimate_none),
                    selected = state.estimateMinutes == null,
                    onClick = { onAction(TaskEditorUiAction.EstimateChanged(null)) },
                )
                EstimatePresets.forEach { minutes ->
                    FlowChip(
                        text = stringResource(R.string.task_editor_estimate_minutes, minutes),
                        selected = state.estimateMinutes == minutes,
                        onClick = { onAction(TaskEditorUiAction.EstimateChanged(minutes)) },
                    )
                }
            }

            EditorSection(title = stringResource(R.string.task_editor_field_due_date)) {
                FlowChip(
                    text = stringResource(R.string.task_editor_due_none),
                    selected = state.dueDate == null,
                    onClick = { onAction(TaskEditorUiAction.DueDateChanged(null)) },
                )
                FlowChip(
                    text = stringResource(R.string.task_editor_due_today),
                    selected = state.dueDate == today,
                    onClick = { onAction(TaskEditorUiAction.DueDateChanged(today)) },
                )
                val tomorrow = today.plus(DatePeriod(days = 1))
                FlowChip(
                    text = stringResource(R.string.task_editor_due_tomorrow),
                    selected = state.dueDate == tomorrow,
                    onClick = { onAction(TaskEditorUiAction.DueDateChanged(tomorrow)) },
                )
                FlowChip(
                    text = state.dueDate
                        ?.takeIf { it != today && it != tomorrow }
                        ?.formatShortDate()
                        ?: stringResource(R.string.task_editor_due_pick),
                    selected = state.dueDate != null && state.dueDate != today && state.dueDate != tomorrow,
                    onClick = { datePickerVisible = true },
                )
            }

            FlowPrimaryButton(
                text = stringResource(R.string.task_editor_save),
                onClick = { onAction(TaskEditorUiAction.Save) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (datePickerVisible) {
        DueDatePickerDialog(
            initialDate = state.dueDate ?: today,
            onDismiss = { datePickerVisible = false },
            onSelect = { date ->
                onAction(TaskEditorUiAction.DueDateChanged(date))
                datePickerVisible = false
            },
        )
    }
}

@Composable
private fun EditorSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8)) {
        Text(text = title, style = FlowTheme.typography.body2, color = FlowTheme.colors.textSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDays() * MILLIS_IN_DAY,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onSelect(LocalDate.fromEpochDays((millis / MILLIS_IN_DAY).toInt()))
                    }
                },
            ) {
                Text(text = stringResource(R.string.task_editor_date_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.task_editor_date_cancel))
            }
        },
        colors = androidx.compose.material3.DatePickerDefaults.colors(
            containerColor = FlowTheme.colors.layer1,
        ),
    ) {
        DatePicker(state = pickerState)
    }
}

private fun Priority.labelRes(): Int =
    when (this) {
        Priority.LOW -> R.string.task_priority_low
        Priority.MEDIUM -> R.string.task_priority_medium
        Priority.HIGH -> R.string.task_priority_high
    }

private const val MILLIS_IN_DAY = 86_400_000L

@Preview
@Composable
private fun TaskEditorScreenPreview() {
    FlowTheme {
        TaskEditorScreen(
            state = TaskEditorUiState(
                isLoading = false,
                title = "Finish Compose navigation",
                description = "Wire up type-safe routes",
                estimateMinutes = 45,
                dueDate = LocalDate(2026, 9, 30),
                categories = listOf(
                    CategoryOptionUi("1", "Work", ru.alexey.flowapp.core.model.AccentColor.PURPLE),
                    CategoryOptionUi("2", "Learning", ru.alexey.flowapp.core.model.AccentColor.BLUE),
                ),
                categoryId = "1",
            ),
            today = LocalDate(2026, 9, 30),
            onAction = {},
            onBack = {},
        )
    }
}