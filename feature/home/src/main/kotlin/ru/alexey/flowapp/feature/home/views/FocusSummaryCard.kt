package ru.alexey.flowapp.feature.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.alexey.flowapp.core.designsystem.component.FlowCard
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.ui.format.formatSignedPercent
import ru.alexey.flowapp.feature.home.R

/**
 * Today focus card
 */
@Composable
internal fun FocusSummaryCard(
    total: String,
    trend: Float?,
    taskProgress: String,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    FlowCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.today_focus_title),
            style = FlowTheme.typography.body2,
            color = colors.textSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(text = total, style = FlowTheme.typography.display, color = colors.textMain)
            if (trend != null) {
                TrendLabel(trend = trend)
            }
        }
        Text(
            text = taskProgress,
            style = FlowTheme.typography.body2,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun TrendLabel(
    trend: Float,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    val positive = trend >= 0f
    val tint = if (positive) colors.success else colors.error

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (positive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
        Text(text = trend.formatSignedPercent(), style = FlowTheme.typography.caption, color = tint)
    }
}