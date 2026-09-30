package ru.alexey.flowapp.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import ru.alexey.flowapp.core.designsystem.component.FlowCheckCircle
import ru.alexey.flowapp.core.designsystem.component.FlowProgressBar
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor

/**
 * Task row shared by Today and Tasks: checkbox, title, "category · estimate" subtitle.
 *
 * @param subtitle e.g. `Work · 45m`, `null` = none
 * @param progress focus progress `0f..1f`, shown while the task is in progress
 * @param trailing optional trailing actions slot
 */
@Composable
fun TaskRow(
    title: String,
    completed: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    progress: Float = 0f,
    accent: AccentColor = AccentColor.Default,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = FlowTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = FlowTheme.spacers.x12),
        horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlowCheckCircle(
            checked = completed,
            onCheckedChange = onToggle,
            contentDescription = title,
            accent = colors.accent(accent),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x2),
        ) {
            Text(
                text = title,
                style = FlowTheme.typography.body1,
                color = if (completed) colors.textTertiary else colors.textMain,
                textDecoration = if (completed) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = FlowTheme.typography.caption,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!completed && progress > 0f) {
                FlowProgressBar(
                    progress = progress,
                    color = colors.accent(accent),
                    height = FlowTheme.spacers.x4,
                    modifier = Modifier.padding(top = FlowTheme.spacers.x4),
                )
            }
        }
        trailing()
    }
}