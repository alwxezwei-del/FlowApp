package ru.alexey.flowapp.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme

/** Segmented control */
@Composable
fun FlowSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FlowTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FlowTheme.radius.x12))
            .background(colors.layer2)
            .padding(FlowTheme.spacers.x4)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x4),
    ) {
        items.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            val background by animateColorAsState(
                targetValue = if (selected) colors.layer3 else colors.layer2,
                label = "segmentBackground",
            )
            Text(
                text = title,
                style = FlowTheme.typography.body2,
                color = if (selected) colors.textMain else colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(FlowTheme.radius.x8))
                    .background(background)
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onSelect(index) },
                    ).padding(vertical = FlowTheme.spacers.x8),
            )
        }
    }
}