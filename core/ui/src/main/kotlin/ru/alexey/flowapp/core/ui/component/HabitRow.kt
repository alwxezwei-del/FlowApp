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
import androidx.compose.ui.text.style.TextOverflow
import ru.alexey.flowapp.core.designsystem.component.FlowCheckCircle
import ru.alexey.flowapp.core.designsystem.component.FlowIconBadge
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor

/**
 * Habit row: icon, name, day progress and checkbox.
 *
 * @param subtitle e.g. `1/1 · 60 min` or `12 day streak`
 */
@Composable
fun HabitRow(
    name: String,
    iconKey: String,
    completed: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accent: AccentColor = AccentColor.Default,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = FlowTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = FlowTheme.spacers.x8),
        horizontalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlowIconBadge(iconKey = iconKey, accent = accent)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x2),
        ) {
            Text(
                text = name,
                style = FlowTheme.typography.body1,
                color = colors.textMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(text = subtitle, style = FlowTheme.typography.caption, color = colors.textSecondary)
            }
        }
        FlowCheckCircle(
            checked = completed,
            onCheckedChange = onToggle,
            contentDescription = name,
            accent = colors.accent(accent),
        )
        trailing()
    }
}