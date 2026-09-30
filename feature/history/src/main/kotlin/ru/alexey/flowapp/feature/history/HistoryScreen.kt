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
import ru.alexey.flowapp.core.designsystem.component.FlowCard
import ru.alexey.flowapp.core.designsystem.component.FlowIconBadge
import ru.alexey.flowapp.core.designsystem.component.FlowSegmentedControl
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.HistoryFilter
import ru.alexey.flowapp.core.ui.component.EmptyState
import ru.alexey.flowapp.core.ui.component.FlowTopBar
import ru.alexey.flowapp.core.ui.component.SectionHeader

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
                item(key = "day_${day.title}") {
                    SectionHeader(title = day.title, modifier = Modifier.fillMaxWidth())
                }
                item(key = "events_${day.title}") {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(text = event.title, style = FlowTheme.typography.body2, color = colors.textMain)
            if (event.subtitle != null) {
                Text(
                    text = event.subtitle,
                    style = FlowTheme.typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
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
                        title = "Today",
                        events = listOf(
                            HistoryEventUi(
                                "1",
                                "14:20",
                                "45m focus",
                                "Finish Compose navigation",
                                HistoryEventKind.FOCUS,
                                "code",
                                AccentColor.PURPLE,
                            ),
                            HistoryEventUi(
                                "2",
                                "12:10",
                                "Walk",
                                "Habit completed",
                                HistoryEventKind.HABIT,
                                "walk",
                                AccentColor.GREEN,
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