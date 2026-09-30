package ru.alexey.flowapp.feature.habits.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import ru.alexey.flowapp.core.designsystem.component.FlowChip
import ru.alexey.flowapp.core.designsystem.component.FlowIconBadge
import ru.alexey.flowapp.core.designsystem.component.FlowPrimaryButton
import ru.alexey.flowapp.core.designsystem.component.FlowTextButton
import ru.alexey.flowapp.core.designsystem.component.FlowTextField
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.FlowIconKey
import ru.alexey.flowapp.core.ui.component.FlowTopBar
import ru.alexey.flowapp.core.ui.format.formatTime
import ru.alexey.flowapp.feature.habits.R

/** Create/edit habit screen */
@Composable
fun HabitEditorScreen(
    state: HabitEditorUiState,
    onAction: (HabitEditorUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    val spacers = FlowTheme.spacers
    var timePickerVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
        topBar = {
            FlowTopBar(
                title = stringResource(
                    if (state.isEditing) R.string.habit_editor_title_edit else R.string.habit_editor_title_create,
                ),
                onBack = onBack,
                actions = {
                    if (state.isEditing) {
                        FlowTextButton(
                            text = stringResource(R.string.habit_editor_delete),
                            onClick = { onAction(HabitEditorUiAction.Delete) },
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
            Section(title = stringResource(R.string.habit_editor_field_icon)) {
                FlowIconKey.All.forEach { icon ->
                    IconOption(
                        icon = icon,
                        accent = state.color,
                        selected = state.icon == icon,
                        onClick = { onAction(HabitEditorUiAction.IconSelected(icon)) },
                    )
                }
            }

            FlowTextField(
                label = stringResource(R.string.habit_editor_field_name),
                value = state.name,
                onValueChange = { onAction(HabitEditorUiAction.NameChanged(it)) },
                placeholder = stringResource(R.string.habit_editor_field_name_hint),
            )

            Section(title = stringResource(R.string.habit_editor_field_color)) {
                AccentColor.entries.forEach { accent ->
                    ColorOption(
                        accent = accent,
                        selected = state.color == accent,
                        onClick = { onAction(HabitEditorUiAction.ColorSelected(accent)) },
                    )
                }
            }

            Section(title = stringResource(R.string.habit_editor_field_schedule)) {
                FlowChip(
                    text = stringResource(R.string.habit_editor_schedule_daily),
                    selected = state.daily,
                    onClick = { onAction(HabitEditorUiAction.ScheduleChanged(true)) },
                )
                FlowChip(
                    text = stringResource(R.string.habit_editor_schedule_selected),
                    selected = !state.daily,
                    onClick = { onAction(HabitEditorUiAction.ScheduleChanged(false)) },
                )
            }

            if (!state.daily) {
                Section(title = "") {
                    DayOfWeek.entries.forEach { day ->
                        FlowChip(
                            text = day.name.take(SHORT_DAY_LENGTH),
                            selected = day in state.selectedDays,
                            onClick = { onAction(HabitEditorUiAction.DayToggled(day)) },
                        )
                    }
                }
            }

            TargetStepper(
                target = state.targetPerDay,
                onChange = { onAction(HabitEditorUiAction.TargetChanged(it)) },
            )

            ReminderRow(
                minuteOfDay = state.reminderMinuteOfDay,
                onToggle = { enabled ->
                    onAction(HabitEditorUiAction.ReminderChanged(if (enabled) DEFAULT_REMINDER_MINUTE else null))
                },
                onPickTime = { timePickerVisible = true },
            )

            FlowTextField(
                label = stringResource(R.string.habit_editor_field_notes),
                value = state.note,
                onValueChange = { onAction(HabitEditorUiAction.NoteChanged(it)) },
                placeholder = stringResource(R.string.habit_editor_notes_hint),
                singleLine = false,
                minLines = 3,
            )

            FlowPrimaryButton(
                text = stringResource(R.string.habit_editor_save),
                onClick = { onAction(HabitEditorUiAction.Save) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (timePickerVisible) {
        ReminderTimeDialog(
            minuteOfDay = state.reminderMinuteOfDay ?: DEFAULT_REMINDER_MINUTE,
            onConfirm = {
                onAction(HabitEditorUiAction.ReminderChanged(it))
                timePickerVisible = false
            },
            onDismiss = { timePickerVisible = false },
        )
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8)) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = FlowTheme.typography.body2,
                color = FlowTheme.colors.textSecondary,
            )
        }
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

@Composable
private fun IconOption(
    icon: String,
    accent: AccentColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = FlowTheme.colors
    FlowIconBadge(
        iconKey = icon,
        accent = accent,
        modifier = Modifier
            .border(
                width = if (selected) 2.dp else Dp.Hairline,
                color = if (selected) colors.accent(accent) else colors.divider,
                shape = androidx.compose.foundation.shape
                    .RoundedCornerShape(FlowTheme.radius.x12),
            ).selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
    )
}

@Composable
private fun ColorOption(
    accent: AccentColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = FlowTheme.colors
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.accent(accent))
            .border(
                width = if (selected) 3.dp else Dp.Hairline,
                color = if (selected) colors.textMain else Color.Transparent,
                shape = CircleShape,
            ).selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}

@Composable
private fun TargetStepper(
    target: Int,
    onChange: (Int) -> Unit,
) {
    val colors = FlowTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8)) {
        Text(
            text = stringResource(R.string.habit_editor_field_target),
            style = FlowTheme.typography.body2,
            color = colors.textSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x12),
        ) {
            IconButton(onClick = { onChange(-1) }) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = stringResource(R.string.habit_editor_target_decrease),
                    tint = colors.iconSecondary,
                )
            }
            Text(
                text = target.toString(),
                style = FlowTheme.typography.title2,
                color = colors.textMain,
            )
            IconButton(onClick = { onChange(1) }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.habit_editor_target_increase),
                    tint = colors.iconSecondary,
                )
            }
            Text(
                text = stringResource(R.string.habit_editor_target_per_day),
                style = FlowTheme.typography.body2,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun ReminderRow(
    minuteOfDay: Int?,
    onToggle: (Boolean) -> Unit,
    onPickTime: () -> Unit,
) {
    val colors = FlowTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8)) {
        Text(
            text = stringResource(R.string.habit_editor_field_reminder),
            style = FlowTheme.typography.body2,
            color = colors.textSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x12),
        ) {
            if (minuteOfDay != null) {
                FlowChip(
                    text = minuteOfDay.toLocalTime().formatTime(),
                    selected = true,
                    onClick = onPickTime,
                )
            }
            Box(modifier = Modifier.weight(1f))
            Switch(
                checked = minuteOfDay != null,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.textOnAccent,
                    checkedTrackColor = colors.primary,
                    uncheckedTrackColor = colors.layer2,
                    uncheckedBorderColor = colors.divider,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    minuteOfDay: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val time = minuteOfDay.toLocalTime()
    val pickerState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FlowTheme.colors.layer1,
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.hour * MINUTES_IN_HOUR + pickerState.minute) }) {
                Text(text = stringResource(ru.alexey.flowapp.core.ui.R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(ru.alexey.flowapp.core.ui.R.string.action_cancel))
            }
        },
    )
}

private fun Int.toLocalTime(): LocalTime = LocalTime(this / MINUTES_IN_HOUR, this % MINUTES_IN_HOUR)

private const val MINUTES_IN_HOUR = 60
private const val SHORT_DAY_LENGTH = 3

/** 19:00 default reminder. */
private const val DEFAULT_REMINDER_MINUTE = 19 * MINUTES_IN_HOUR

@Preview
@Composable
private fun HabitEditorScreenPreview() {
    FlowTheme {
        HabitEditorScreen(
            state = HabitEditorUiState(
                isLoading = false,
                name = "Code",
                icon = FlowIconKey.CODE,
                color = AccentColor.PINK,
                reminderMinuteOfDay = DEFAULT_REMINDER_MINUTE,
            ),
            onAction = {},
            onBack = {},
        )
    }
}