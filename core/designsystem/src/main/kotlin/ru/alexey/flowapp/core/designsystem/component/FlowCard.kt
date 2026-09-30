package ru.alexey.flowapp.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme

/**
 * Base card
 */
@Composable
fun FlowCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = FlowTheme.spacers.x16,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = FlowTheme.colors
    val shape = RoundedCornerShape(FlowTheme.radius.x18)

    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.layer1)
            .border(BorderStroke(width = Dp.Hairline, color = colors.divider), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}