package ru.alexey.flowapp.feature.home.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.ui.format.dayInitial
import ru.alexey.flowapp.core.ui.format.formatFullDate

/**
 * Week strip in the header
 */
@Composable
internal fun WeekStrip(
    days: List<LocalDate>,
    selected: LocalDate,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        days.forEach { day ->
            val isSelected = day == selected
            val background by animateColorAsState(
                targetValue = if (isSelected) colors.primary else Color.Transparent,
                label = "dayBackground",
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x6),
                modifier = Modifier
                    .clip(CircleShape)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onSelect(day) },
                    ).clearAndSetSemantics { contentDescription = day.formatFullDate() }
                    .padding(FlowTheme.spacers.x4),
            ) {
                Text(
                    text = day.dayInitial(),
                    style = FlowTheme.typography.caption,
                    color = colors.textTertiary,
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(background),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = day.day.toString(),
                        style = FlowTheme.typography.body2,
                        color = when {
                            isSelected -> colors.textOnAccent
                            day == today -> colors.primary
                            else -> colors.textMain
                        },
                    )
                }
            }
        }
    }
}