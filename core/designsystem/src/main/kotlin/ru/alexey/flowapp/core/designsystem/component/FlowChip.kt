package ru.alexey.flowapp.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme

/** Selection chip */
@Composable
fun FlowChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = FlowTheme.colors.primary,
) {
    val colors = FlowTheme.colors
    val shape = RoundedCornerShape(FlowTheme.radius.x12)
    val background by animateColorAsState(
        targetValue = if (selected) accent else colors.layer2,
        label = "chipBackground",
    )

    Text(
        text = text,
        style = FlowTheme.typography.body2,
        color = if (selected) colors.textOnAccent else colors.textSecondary,
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(BorderStroke(Dp.Hairline, if (selected) accent else colors.divider), shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = FlowTheme.spacers.x12, vertical = FlowTheme.spacers.x8),
    )
}