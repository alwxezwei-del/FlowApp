package ru.alexey.flowapp.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme

/** Primary action button */
@Composable
fun FlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val colors = FlowTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(FlowTheme.radius.x14),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.textOnAccent,
            disabledContainerColor = colors.layer2,
            disabledContentColor = colors.textTertiary,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null)
            }
            Text(text = text, style = FlowTheme.typography.button)
        }
    }
}

/** Secondary button on layer2 surface */
@Composable
fun FlowSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = FlowTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(FlowTheme.radius.x14),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.layer2,
            contentColor = colors.textMain,
            disabledContainerColor = colors.layer2,
            disabledContentColor = colors.textTertiary,
        ),
    ) {
        Text(text = text, style = FlowTheme.typography.button)
    }
}

/** Text button for destructive and minor actions */
@Composable
fun FlowTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    val colors = FlowTheme.colors
    TextButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            style = FlowTheme.typography.button,
            color = if (destructive) colors.error else colors.textSecondary,
        )
    }
}