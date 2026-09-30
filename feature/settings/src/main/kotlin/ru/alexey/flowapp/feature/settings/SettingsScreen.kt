package ru.alexey.flowapp.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import ru.alexey.flowapp.core.designsystem.component.FlowCard
import ru.alexey.flowapp.core.designsystem.component.FlowChip
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.ThemeMode
import ru.alexey.flowapp.core.ui.component.FlowTopBar
import ru.alexey.flowapp.core.ui.component.SectionHeader

/** Settings */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsUiAction) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    val spacers = FlowTheme.spacers

    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
        topBar = { FlowTopBar(title = stringResource(R.string.settings_title), onBack = onBack) },
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
            item(key = "timer_header") {
                SectionHeader(title = stringResource(R.string.settings_section_timer), modifier = Modifier.fillMaxWidth())
            }
            item(key = "timer") {
                FlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacers.x16)) {
                        ChipRow(
                            title = stringResource(R.string.settings_focus_duration),
                            values = DurationPresets.Focus,
                            selected = state.focusMinutes,
                            label = { stringResource(R.string.settings_minutes, it) },
                            onSelect = { onAction(SettingsUiAction.FocusDurationChanged(it)) },
                        )
                        ChipRow(
                            title = stringResource(R.string.settings_short_break),
                            values = DurationPresets.ShortBreak,
                            selected = state.shortBreakMinutes,
                            label = { stringResource(R.string.settings_minutes, it) },
                            onSelect = { onAction(SettingsUiAction.ShortBreakChanged(it)) },
                        )
                        ChipRow(
                            title = stringResource(R.string.settings_long_break),
                            values = DurationPresets.LongBreak,
                            selected = state.longBreakMinutes,
                            label = { stringResource(R.string.settings_minutes, it) },
                            onSelect = { onAction(SettingsUiAction.LongBreakChanged(it)) },
                        )
                    }
                }
            }

            item(key = "appearance_header") {
                SectionHeader(
                    title = stringResource(R.string.settings_section_appearance),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "appearance") {
                FlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacers.x16)) {
                        ChipRow(
                            title = stringResource(R.string.settings_theme),
                            values = DurationPresets.Themes,
                            selected = state.themeMode,
                            label = { stringResource(it.labelRes()) },
                            onSelect = { onAction(SettingsUiAction.ThemeSelected(it)) },
                        )
                        AccentRow(
                            selected = state.accentColor,
                            onSelect = { onAction(SettingsUiAction.AccentSelected(it)) },
                        )
                        ChipRow(
                            title = stringResource(R.string.settings_start_of_week),
                            values = DurationPresets.StartOfWeek,
                            selected = state.startOfWeek,
                            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                            onSelect = { onAction(SettingsUiAction.StartOfWeekChanged(it)) },
                        )
                        SwitchRow(
                            title = stringResource(R.string.settings_notifications),
                            subtitle = stringResource(R.string.settings_notifications_subtitle),
                            checked = state.notificationsEnabled,
                            onCheckedChange = { onAction(SettingsUiAction.NotificationsToggled(it)) },
                        )
                    }
                }
            }

            item(key = "data_header") {
                SectionHeader(title = stringResource(R.string.settings_section_data), modifier = Modifier.fillMaxWidth())
            }
            item(key = "data") {
                FlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacers.x4)) {
                        ActionRow(
                            title = stringResource(R.string.settings_export),
                            subtitle = stringResource(R.string.settings_export_subtitle),
                            onClick = onExport,
                        )
                        ActionRow(
                            title = stringResource(R.string.settings_import),
                            subtitle = stringResource(R.string.settings_import_subtitle),
                            onClick = onImport,
                        )
                        ActionRow(
                            title = stringResource(R.string.settings_reset),
                            subtitle = stringResource(R.string.settings_reset_subtitle),
                            destructive = true,
                            onClick = { onAction(SettingsUiAction.ResetRequested) },
                        )
                    }
                }
            }

            item(key = "about") {
                FlowCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_about_app),
                        style = FlowTheme.typography.body1,
                        color = colors.textMain,
                    )
                    Text(
                        text = stringResource(R.string.settings_about_version, state.appVersion),
                        style = FlowTheme.typography.caption,
                        color = colors.textSecondary,
                    )
                    Text(
                        text = stringResource(R.string.settings_about_offline),
                        style = FlowTheme.typography.caption,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = spacers.x8),
                    )
                }
            }
        }
    }

    if (state.resetDialogVisible) {
        ResetDialog(
            onConfirm = { onAction(SettingsUiAction.ResetConfirmed) },
            onDismiss = { onAction(SettingsUiAction.ResetDismissed) },
        )
    }
}

@Composable
private fun <T> ChipRow(
    title: String,
    values: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8)) {
        Text(text = title, style = FlowTheme.typography.body2, color = FlowTheme.colors.textSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8),
        ) {
            values.forEach { value ->
                FlowChip(
                    text = label(value),
                    selected = value == selected,
                    onClick = { onSelect(value) },
                )
            }
        }
    }
}

@Composable
private fun AccentRow(
    selected: AccentColor,
    onSelect: (AccentColor) -> Unit,
) {
    val colors = FlowTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8)) {
        Text(
            text = stringResource(R.string.settings_accent),
            style = FlowTheme.typography.body2,
            color = colors.textSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8)) {
            AccentColor.entries.forEach { accent ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.accent(accent))
                        .border(
                            width = if (accent == selected) 3.dp else Dp.Hairline,
                            color = if (accent == selected) colors.textMain else Color.Transparent,
                            shape = CircleShape,
                        ).selectable(
                            selected = accent == selected,
                            role = Role.RadioButton,
                            onClick = { onSelect(accent) },
                        ),
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = FlowTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x12),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = FlowTheme.typography.body1, color = colors.textMain)
            Text(text = subtitle, style = FlowTheme.typography.caption, color = colors.textSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.textOnAccent,
                checkedTrackColor = colors.primary,
                uncheckedTrackColor = colors.layer2,
                uncheckedBorderColor = colors.divider,
            ),
        )
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    val colors = FlowTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = FlowTheme.spacers.x12),
    ) {
        Text(
            text = title,
            style = FlowTheme.typography.body1,
            color = if (destructive) colors.error else colors.textMain,
        )
        Text(text = subtitle, style = FlowTheme.typography.caption, color = colors.textSecondary)
    }
}

@Composable
private fun ResetDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FlowTheme.colors.layer1,
        title = {
            Text(
                text = stringResource(R.string.settings_reset_dialog_title),
                style = FlowTheme.typography.title2,
                color = FlowTheme.colors.textMain,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.settings_reset_dialog_message),
                style = FlowTheme.typography.body2,
                color = FlowTheme.colors.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.settings_reset_dialog_confirm),
                    color = FlowTheme.colors.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(ru.alexey.flowapp.core.ui.R.string.action_cancel))
            }
        },
    )
}

private fun ThemeMode.labelRes(): Int =
    when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }

@Preview
@Composable
private fun SettingsScreenPreview() {
    FlowTheme {
        SettingsScreen(
            state = SettingsUiState(isLoading = false, appVersion = "1.0.0", startOfWeek = DayOfWeek.MONDAY),
            onAction = {},
            onExport = {},
            onImport = {},
            onBack = {},
        )
    }
}