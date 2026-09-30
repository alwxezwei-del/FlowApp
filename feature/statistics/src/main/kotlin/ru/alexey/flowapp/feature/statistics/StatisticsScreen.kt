package ru.alexey.flowapp.feature.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.alexey.flowapp.core.designsystem.component.FlowBar
import ru.alexey.flowapp.core.designsystem.component.FlowBarChart
import ru.alexey.flowapp.core.designsystem.component.FlowCard
import ru.alexey.flowapp.core.designsystem.component.FlowIconBadge
import ru.alexey.flowapp.core.designsystem.component.FlowProgressBar
import ru.alexey.flowapp.core.designsystem.component.FlowSegmentedControl
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.StatisticsPeriod
import ru.alexey.flowapp.core.ui.component.EmptyState
import ru.alexey.flowapp.core.ui.component.FlowTopBar
import ru.alexey.flowapp.core.ui.component.SectionHeader
import ru.alexey.flowapp.core.ui.format.formatPercent
import ru.alexey.flowapp.core.ui.format.formatSignedPercent

/** Statistics */
@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onAction: (StatisticsUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    val spacers = FlowTheme.spacers

    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
        topBar = { FlowTopBar(title = stringResource(R.string.statistics_title)) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + spacers.x8,
                bottom = padding.calculateBottomPadding() + spacers.x24,
                start = spacers.x16,
                end = spacers.x16,
            ),
            verticalArrangement = Arrangement.spacedBy(spacers.x16),
        ) {
            item(key = "period") {
                FlowSegmentedControl(
                    items = StatisticsPeriod.entries.map { stringResource(it.labelRes()) },
                    selectedIndex = StatisticsPeriod.entries.indexOf(state.period),
                    onSelect = { onAction(StatisticsUiAction.SelectPeriod(StatisticsPeriod.entries[it])) },
                )
            }

            if (state.isEmpty && !state.isLoading) {
                item(key = "empty") {
                    FlowCard {
                        EmptyState(
                            title = stringResource(R.string.statistics_empty_title),
                            subtitle = stringResource(R.string.statistics_empty_subtitle),
                        )
                    }
                }
                return@LazyColumn
            }

            item(key = "focus") {
                FlowCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.statistics_focused),
                        style = FlowTheme.typography.body2,
                        color = colors.textSecondary,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacers.x8),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = state.totalFocus,
                            style = FlowTheme.typography.display,
                            color = colors.textMain,
                        )
                        state.focusTrend?.let { trend ->
                            Text(
                                text = trend.formatSignedPercent(),
                                style = FlowTheme.typography.caption,
                                color = if (trend >= 0f) colors.success else colors.error,
                            )
                        }
                    }
                    FlowBarChart(
                        bars = state.bars.map {
                            FlowBar(
                                label = it.label,
                                value = it.minutes,
                                description = it.description,
                                highlighted = it.highlighted,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item(key = "completion") {
                FlowCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.statistics_completion),
                        style = FlowTheme.typography.body2,
                        color = colors.textSecondary,
                    )
                    Text(
                        text = state.completionRate.formatPercent(),
                        style = FlowTheme.typography.display,
                        color = colors.textMain,
                    )
                    Text(
                        text = stringResource(
                            R.string.statistics_completion_detail,
                            state.completedTasks,
                            state.totalTasks,
                        ),
                        style = FlowTheme.typography.caption,
                        color = colors.textSecondary,
                    )
                    FlowProgressBar(
                        progress = state.completionRate,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            distributionSection(
                titleRes = R.string.statistics_focus_areas,
                rows = state.categories,
            )
            distributionSection(
                titleRes = R.string.statistics_habit_consistency,
                rows = state.habits,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.distributionSection(
    @androidx.annotation.StringRes titleRes: Int,
    rows: List<DistributionRowUi>,
) {
    if (rows.isEmpty()) return

    item(key = "header_$titleRes") {
        SectionHeader(title = stringResource(titleRes), modifier = Modifier.fillMaxWidth())
    }
    item(key = "rows_$titleRes") {
        FlowCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x12)) {
                rows.forEach { row -> DistributionRow(row = row) }
            }
        }
    }
}

@Composable
private fun DistributionRow(row: DistributionRowUi) {
    val colors = FlowTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (row.icon != null) {
            FlowIconBadge(iconKey = row.icon, accent = row.color, size = FlowTheme.spacers.x32)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x6),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = row.title,
                    style = FlowTheme.typography.body2,
                    color = colors.textMain,
                    modifier = Modifier.weight(1f),
                )
                Text(text = row.value, style = FlowTheme.typography.body2, color = colors.textSecondary)
            }
            FlowProgressBar(
                progress = row.share,
                color = colors.accent(row.color),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun StatisticsPeriod.labelRes(): Int =
    when (this) {
        StatisticsPeriod.WEEK -> R.string.statistics_period_week
        StatisticsPeriod.MONTH -> R.string.statistics_period_month
        StatisticsPeriod.YEAR -> R.string.statistics_period_year
    }

@Preview
@Composable
private fun StatisticsScreenPreview() {
    FlowTheme {
        StatisticsScreen(
            state = StatisticsUiState(
                isLoading = false,
                totalFocus = "8h 42m",
                focusTrend = 0.12f,
                bars = listOf("M", "T", "W", "T", "F", "S", "S").mapIndexed { index, label ->
                    FocusBarUi(label, (index + 1) * 20f, "$label, 20m", index == 2)
                },
                completionRate = 0.78f,
                completedTasks = 18,
                totalTasks = 23,
                categories = listOf(
                    DistributionRowUi("1", "Work", "4h 30m", 0.52f, AccentColor.PURPLE),
                    DistributionRowUi("2", "Learning", "2h 40m", 0.31f, AccentColor.BLUE),
                ),
                habits = listOf(
                    DistributionRowUi("h1", "Code", "5/7", 0.71f, AccentColor.PINK, "code"),
                    DistributionRowUi("h2", "Read", "4/7", 0.57f, AccentColor.BLUE, "read"),
                ),
            ),
            onAction = {},
        )
    }
}