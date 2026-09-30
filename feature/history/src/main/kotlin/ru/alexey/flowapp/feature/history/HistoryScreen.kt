package ru.alexey.flowapp.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.designsystem.component.FlowCard
import ru.alexey.flowapp.core.designsystem.component.FlowIconBadge
import ru.alexey.flowapp.core.designsystem.component.FlowSegmentedControl
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.HistoryFilter
import ru.alexey.flowapp.core.ui.component.EmptyState
import ru.alexey.flowapp.core.ui.component.FlowTopBar
import ru.alexey.flowapp.core.ui.component.SectionHeader
import ru.alexey.flowapp.core.ui.format.formatShortDate

/** History feed */
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onAction: (HistoryUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    val spacers = FlowTheme.spacers

    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
        topBar = { FlowTopBar(title = stringResource(R.string.history_title), onBack = onBack) },
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
            item(key = "filter") {
                FlowSegmentedControl(
                    items = HistoryFilter.entries.map { stringResource(it.labelRes()) },
                    selectedIndex = HistoryFilter.entries.indexOf(state.filter),
                    onSelect = { onAction(HistoryUiAction.SelectFilter(HistoryFilter.entries[it])) },
                )
            }

            if (state.days.isEmpty() && !state.isLoading) {
                item(key = "empty") {
                    FlowCard {
                        EmptyState(
                            title = stringResource(R.string.history_empty_title),
                            subtitle = stringResource(R.string.history_empty_subtitle),
                        )
                    }
                }
            }

            state.days.forEach { day ->
                item(key = "day_${day.date}") {
                    SectionHeader(title = day.title(), modifier = Modifier.fillMaxWidth())
                }
                item(key = "events_${day.date}") {
                    FlowCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacers.x12)) {
                            day.events.forEach { event -> HistoryRow(event = event) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(event: HistoryEventUi) {
    val colors = FlowTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = event.time,
            style = FlowTheme.typography.caption,
            color = colors.textTertiary,
            modifier = Modifier.width(52.dp),
        )
        FlowIconBadge(iconKey = event.icon, accent = event.accent, size = FlowTheme.spacers.x32)
        val (title, subtitle) = event.content.texts()
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = FlowTheme.typography.body2, color = colors.textMain)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = FlowTheme.typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun HistoryDayUi.title(): String =
    when (relative) {
        RelativeDay.TODAY -> stringResource(R.string.history_today)
        RelativeDay.YESTERDAY -> stringResource(R.string.history_yesterday)
        null -> date.formatShortDate()
    }

/** Title and optional subtitle of a feed row */
@Composable
private fun HistoryEventContent.texts(): Pair<String, String?> =
    when (this) {
        is HistoryEventContent.Focus -> {
            val titleRes = when (kind) {
                FocusKind.FOCUS -> R.string.history_focus
                FocusKind.SHORT_BREAK -> R.string.history_short_break
                FocusKind.LONG_BREAK -> R.string.history_long_break
            }
            val interruptedLabel = stringResource(R.string.history_interrupted).takeIf { interrupted }
            val subtitle = listOfNotNull(taskTitle, interruptedLabel)
                .joinToString(" · ")
                .takeIf { it.isNotEmpty() }
            stringResource(titleRes, duration) to subtitle
        }

        is HistoryEventContent.Task -> {
            title to categoryName
        }

        is HistoryEventContent.Habit -> {
            name to stringResource(R.string.history_habit_completed)
        }
    }

private fun HistoryFilter.labelRes(): Int =
    when (this) {
        HistoryFilter.ALL -> R.string.history_filter_all
        HistoryFilter.FOCUS -> R.string.history_filter_focus
        HistoryFilter.TASKS -> R.string.history_filter_tasks
        HistoryFilter.HABITS -> R.string.history_filter_habits
    }

@Preview
@Composable
private fun HistoryScreenPreview() {
    FlowTheme {
        HistoryScreen(
            state = HistoryUiState(
                isLoading = false,
                days = listOf(
                    HistoryDayUi(
                        date = LocalDate(2026, 9, 30),
                        relative = RelativeDay.TODAY,
                        events = listOf(
                            HistoryEventUi(
                                id = "1",
                                time = "14:20",
                                icon = "code",
                                accent = AccentColor.PURPLE,
                                content = HistoryEventContent.Focus(
                                    duration = "45m",
                                    kind = FocusKind.FOCUS,
                                    taskTitle = "Finish Compose navigation",
                                    interrupted = false,
                                ),
                            ),
                            HistoryEventUi(
                                id = "2",
                                time = "12:10",
                                icon = "walk",
                                accent = AccentColor.GREEN,
                                content = HistoryEventContent.Habit(name = "Walk"),
                            ),
                        ),
                    ),
                ),
            ),
            onAction = {},
            onBack = {},
        )
    }
}