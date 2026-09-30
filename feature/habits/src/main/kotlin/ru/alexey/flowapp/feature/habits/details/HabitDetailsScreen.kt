package ru.alexey.flowapp.feature.habits.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import ru.alexey.flowapp.core.designsystem.component.FlowCard
import ru.alexey.flowapp.core.designsystem.component.FlowIconBadge
import ru.alexey.flowapp.core.designsystem.component.FlowSecondaryButton
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.ui.component.FlowTopBar
import ru.alexey.flowapp.core.ui.format.formatPercent
import ru.alexey.flowapp.feature.habits.R

private const val GRID_COLUMNS = 10

/**
 * Habit details
 */
@Composable
fun HabitDetailsScreen(
    state: HabitDetailsUiState,
    onAction: (HabitDetailsUiAction) -> Unit,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    val spacers = FlowTheme.spacers

    Scaffold(
        modifier = modifier,
        containerColor = colors.layer0,
        topBar = { FlowTopBar(title = state.name, onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacers.x16),
            verticalArrangement = Arrangement.spacedBy(spacers.x16),
        ) {
            FlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacers.x12),
                ) {
                    FlowIconBadge(iconKey = state.icon, accent = state.accent)
                    Column {
                        Text(text = state.name, style = FlowTheme.typography.title2, color = colors.textMain)
                        Text(
                            text = state.scheduleLabel,
                            style = FlowTheme.typography.caption,
                            color = colors.textSecondary,
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(spacers.x12)) {
                StatTile(
                    title = stringResource(R.string.habit_details_current_streak),
                    value = stringResource(R.string.habit_details_days, state.currentStreak),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    title = stringResource(R.string.habit_details_longest_streak),
                    value = stringResource(R.string.habit_details_days, state.longestStreak),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    title = stringResource(R.string.habit_details_completion_rate),
                    value = state.completionRate.formatPercent(),
                    modifier = Modifier.weight(1f),
                )
            }

            FlowCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.habit_details_last_30_days),
                    style = FlowTheme.typography.body2,
                    color = colors.textSecondary,
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_COLUMNS),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacers.x12),
                    horizontalArrangement = Arrangement.spacedBy(spacers.x6),
                    verticalArrangement = Arrangement.spacedBy(spacers.x6),
                ) {
                    items(items = state.days, key = { it.date.toString() }) { day ->
                        DayCell(
                            day = day,
                            accent = state.accent,
                            onClick = { onAction(HabitDetailsUiAction.ToggleDay(day.date, !day.completed)) },
                        )
                    }
                }
            }

            FlowSecondaryButton(
                text = stringResource(R.string.habit_details_edit),
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    FlowCard(modifier = modifier, contentPadding = FlowTheme.spacers.x12) {
        Text(
            text = title,
            style = FlowTheme.typography.caption,
            color = colors.textSecondary,
            textAlign = TextAlign.Start,
        )
        Text(text = value, style = FlowTheme.typography.title2, color = colors.textMain)
    }
}

@Composable
private fun DayCell(
    day: HabitDayUi,
    accent: AccentColor,
    onClick: () -> Unit,
) {
    val colors = FlowTheme.colors
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(FlowTheme.radius.x8))
            .background(
                when {
                    day.completed -> colors.accent(accent)
                    day.scheduled -> colors.layer2
                    else -> colors.layer1
                },
            ).border(
                width = Dp.Hairline,
                color = colors.divider,
                shape = RoundedCornerShape(FlowTheme.radius.x8),
            ).clickable(onClick = onClick),
    )
}

@Preview
@Composable
private fun HabitDetailsScreenPreview() {
    FlowTheme {
        HabitDetailsScreen(
            state = HabitDetailsUiState(
                isLoading = false,
                name = "Read",
                icon = "read",
                accent = AccentColor.BLUE,
                scheduleLabel = "Every day",
                currentStreak = 4,
                longestStreak = 21,
                completionRate = 0.87f,
                days = List(30) {
                    HabitDayUi(LocalDate(2026, 9, 1).plus(DatePeriod(days = it)), true, it % 3 != 0)
                },
            ),
            onAction = {},
            onEdit = {},
            onBack = {},
        )
    }
}