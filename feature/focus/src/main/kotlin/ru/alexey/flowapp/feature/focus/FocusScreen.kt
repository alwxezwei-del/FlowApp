package ru.alexey.flowapp.feature.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.alexey.flowapp.core.designsystem.component.FlowChip
import ru.alexey.flowapp.core.designsystem.component.FlowPrimaryButton
import ru.alexey.flowapp.core.designsystem.component.FlowProgressRing
import ru.alexey.flowapp.core.designsystem.component.FlowSecondaryButton
import ru.alexey.flowapp.core.designsystem.component.FlowTextButton
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.feature.focus.views.CustomDurationDialog
import ru.alexey.flowapp.feature.focus.views.TaskPickerSheet

/** Focus screen.*/
@Composable
fun FocusScreen(
    state: FocusUiState,
    onAction: (FocusUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    val spacers = FlowTheme.spacers

    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacers.x24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacers.x24, Alignment.CenterVertically),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacers.x4),
            ) {
                Text(
                    text = stringResource(state.kind.titleRes()),
                    style = FlowTheme.typography.title1,
                    color = colors.textMain,
                )
                Text(
                    text = stringResource(R.string.focus_subtitle),
                    style = FlowTheme.typography.body2,
                    color = colors.textSecondary,
                )
            }

            FlowProgressRing(
                progress = state.progress,
                modifier = Modifier.size(260.dp),
                strokeWidth = 12.dp,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacers.x4),
                ) {
                    Text(
                        text = state.timeLabel,
                        style = FlowTheme.typography.timer,
                        color = colors.textMain,
                    )
                    Text(
                        text = state.selectedTaskTitle ?: stringResource(R.string.focus_free_session),
                        style = FlowTheme.typography.body2,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (!state.isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacers.x8, Alignment.CenterHorizontally),
                ) {
                    state.presets.forEach { preset ->
                        FlowChip(
                            text = stringResource(preset.kind.presetLabelRes(), preset.minutes),
                            selected = state.kind == preset.kind && state.selectedMinutes == preset.minutes,
                            onClick = { onAction(FocusUiAction.SelectPreset(preset)) },
                        )
                    }
                    FlowChip(
                        text = stringResource(R.string.focus_custom_duration),
                        selected = state.presets.none { it.minutes == state.selectedMinutes },
                        onClick = { onAction(FocusUiAction.OpenDurationPicker) },
                    )
                }

                FlowSecondaryButton(
                    text = state.selectedTaskTitle ?: stringResource(R.string.focus_pick_task),
                    onClick = { onAction(FocusUiAction.OpenTaskPicker) },
                    modifier = Modifier.fillMaxWidth(),
                )

                FlowPrimaryButton(
                    text = stringResource(R.string.focus_start),
                    onClick = { onAction(FocusUiAction.Start) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                FlowPrimaryButton(
                    text = stringResource(
                        if (state.isPaused) R.string.focus_resume else R.string.focus_pause,
                    ),
                    onClick = { onAction(FocusUiAction.TogglePause) },
                    modifier = Modifier.fillMaxWidth(),
                )

                FlowTextButton(
                    text = stringResource(R.string.focus_end_session),
                    onClick = { onAction(FocusUiAction.Stop) },
                )
            }
        }
    }

    if (state.taskPickerVisible) {
        TaskPickerSheet(
            tasks = state.tasks,
            selectedTaskId = state.selectedTaskId,
            onSelect = { onAction(FocusUiAction.SelectTask(it)) },
            onDismiss = { onAction(FocusUiAction.DismissPicker) },
        )
    }

    if (state.durationPickerVisible) {
        CustomDurationDialog(
            initialMinutes = state.selectedMinutes,
            onConfirm = { onAction(FocusUiAction.SelectCustomDuration(it)) },
            onDismiss = { onAction(FocusUiAction.DismissPicker) },
        )
    }
}

private fun FocusKind.titleRes(): Int =
    when (this) {
        FocusKind.FOCUS -> R.string.focus_kind_focus
        FocusKind.SHORT_BREAK -> R.string.focus_kind_short_break
        FocusKind.LONG_BREAK -> R.string.focus_kind_long_break
    }

@Preview
@Composable
private fun FocusScreenPreview() {
    FlowTheme {
        FocusScreen(
            state = FocusUiState(
                timeLabel = "25:00",
                progress = 0.35f,
                selectedTaskTitle = "Finish Compose navigation",
                presets = listOf(
                    FocusPresetUi(FocusKind.FOCUS, 25),
                    FocusPresetUi(FocusKind.SHORT_BREAK, 5),
                    FocusPresetUi(FocusKind.LONG_BREAK, 15),
                ),
                selectedMinutes = 25,
            ),
            onAction = {},
        )
    }
}

private fun FocusKind.presetLabelRes(): Int =
    when (this) {
        FocusKind.FOCUS -> R.string.focus_preset_minutes
        FocusKind.SHORT_BREAK -> R.string.focus_preset_short_break
        FocusKind.LONG_BREAK -> R.string.focus_preset_long_break
    }