package ru.alexey.flowapp.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale on a 4dp grid */
@Immutable
data class FlowSpacers(
    val x2: Dp = 2.dp,
    val x4: Dp = 4.dp,
    val x6: Dp = 6.dp,
    val x8: Dp = 8.dp,
    val x12: Dp = 12.dp,
    val x16: Dp = 16.dp,
    val x20: Dp = 20.dp,
    val x24: Dp = 24.dp,
    val x32: Dp = 32.dp,
)

/** Corner radius scale. Cards use 18dp */
@Immutable
data class FlowRadius(
    val x8: Dp = 8.dp,
    val x12: Dp = 12.dp,
    val x14: Dp = 14.dp,
    val x18: Dp = 18.dp,
    val x24: Dp = 24.dp,
)