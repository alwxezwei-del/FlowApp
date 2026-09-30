package ru.alexey.flowapp.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alexey.flowapp.core.designsystem.icon.FlowIcons
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme

/** Round checkbox for tasks and habits */
@Composable
fun FlowCheckCircle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 26.dp,
    accent: Color = FlowTheme.colors.primary,
) {
    val colors = FlowTheme.colors
    val background by animateColorAsState(
        targetValue = if (checked) accent else Color.Transparent,
        label = "checkBackground",
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(BorderStroke(width = 2.dp, color = if (checked) accent else colors.divider), CircleShape)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = FlowIcons.Check,
                contentDescription = contentDescription,
                tint = colors.textOnAccent,
                modifier = Modifier.size(size * 0.6f),
            )
        }
    }
}