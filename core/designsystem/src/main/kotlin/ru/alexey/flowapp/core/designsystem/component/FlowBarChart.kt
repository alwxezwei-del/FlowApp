package ru.alexey.flowapp.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme

/**
 * Chart bar
 */
data class FlowBar(
    val label: String,
    val value: Float,
    val description: String,
    val highlighted: Boolean = false,
)

/** Daily focus bar chart */
@Composable
fun FlowBarChart(
    bars: List<FlowBar>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 120.dp,
    color: Color = FlowTheme.colors.primary,
) {
    val colors = FlowTheme.colors
    val max = bars.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x6),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { bar ->
            val fraction by animateFloatAsState(
                targetValue = (bar.value / max).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 400),
                label = "barFraction",
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = bar.description },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x6),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction.coerceAtLeast(MIN_VISIBLE_FRACTION))
                            .clip(RoundedCornerShape(FlowTheme.radius.x8))
                            .background(if (bar.highlighted) color else color.copy(alpha = INACTIVE_ALPHA)),
                    )
                }
                Text(
                    text = bar.label,
                    style = FlowTheme.typography.caption,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

/** Empty days still render a thin bar */
private const val MIN_VISIBLE_FRACTION = 0.02f
private const val INACTIVE_ALPHA = 0.55f