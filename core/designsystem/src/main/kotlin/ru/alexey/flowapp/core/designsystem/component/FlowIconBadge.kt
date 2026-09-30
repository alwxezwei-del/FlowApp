package ru.alexey.flowapp.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alexey.flowapp.core.designsystem.icon.FlowIcons
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.model.AccentColor

/** Square habit/category icon on a tinted background */
@Composable
fun FlowIconBadge(
    iconKey: String,
    accent: AccentColor,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val colors = FlowTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(FlowTheme.radius.x12))
            .background(colors.accentSurface(accent)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = FlowIcons.byKey(iconKey),
            contentDescription = null,
            tint = colors.accent(accent),
            modifier = Modifier.size(size / 2),
        )
    }
}