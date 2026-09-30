package ru.alexey.flowapp.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme

/** Empty list state. */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = FlowTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = FlowTheme.spacers.x32, horizontal = FlowTheme.spacers.x24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x8),
    ) {
        Text(
            text = title,
            style = FlowTheme.typography.title2,
            color = colors.textMain,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = FlowTheme.typography.body2,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}