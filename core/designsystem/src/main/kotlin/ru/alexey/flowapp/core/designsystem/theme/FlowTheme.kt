package ru.alexey.flowapp.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.ThemeMode

/** Design system tokens accessor */
object FlowTheme {
    val colors: FlowColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalFlowColorScheme.current

    val typography: FlowTypography
        @Composable @ReadOnlyComposable
        get() = LocalFlowTypography.current

    val spacers: FlowSpacers
        @Composable @ReadOnlyComposable
        get() = LocalFlowSpacers.current

    val radius: FlowRadius
        @Composable @ReadOnlyComposable
        get() = LocalFlowRadius.current
}

/**
 * App theme
 */
@Composable
fun FlowTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.Default,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val base = if (dark) DarkFlowColorScheme else LightFlowColorScheme
    val colors = base.copy(primary = base.accent(accentColor))

    CompositionLocalProvider(
        LocalFlowColorScheme provides colors,
        LocalFlowTypography provides DefaultFlowTypography,
        LocalFlowSpacers provides FlowSpacers(),
        LocalFlowRadius provides FlowRadius(),
        LocalTextStyle provides DefaultFlowTypography.body1,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(dark),
            content = content,
        )
    }
}

private fun FlowColorScheme.toMaterialScheme(dark: Boolean) =
    (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = primary,
        onPrimary = textOnAccent,
        background = layer0,
        onBackground = textMain,
        surface = layer1,
        onSurface = textMain,
        surfaceVariant = layer2,
        onSurfaceVariant = textSecondary,
        outline = divider,
        outlineVariant = divider,
        error = error,
        scrim = scrim,
    )

internal val LocalFlowColorScheme = staticCompositionLocalOf { DarkFlowColorScheme }
internal val LocalFlowTypography = staticCompositionLocalOf { DefaultFlowTypography }
internal val LocalFlowSpacers = staticCompositionLocalOf { FlowSpacers() }
internal val LocalFlowRadius = staticCompositionLocalOf { FlowRadius() }