package ru.alexey.flowapp.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/** Role based typography */
@Immutable
data class FlowTypography(
    val display: TextStyle,
    val title1: TextStyle,
    val title2: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,
    val timer: TextStyle,
)

private val lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

internal val DefaultFlowTypography = FlowTypography(
    display = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Medium,
        lineHeightStyle = lineHeightStyle,
    ),
    title1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeightStyle = lineHeightStyle,
    ),
    title2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeightStyle = lineHeightStyle,
    ),
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
        lineHeightStyle = lineHeightStyle,
    ),
    body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        lineHeightStyle = lineHeightStyle,
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Normal,
        lineHeightStyle = lineHeightStyle,
    ),
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeightStyle = lineHeightStyle,
    ),
    timer = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 56.sp,
        lineHeight = 62.sp,
        fontWeight = FontWeight.Medium,
        lineHeightStyle = lineHeightStyle,
    ),
)