package ru.alexey.flowapp.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import ru.alexey.flowapp.core.model.AccentColor

/** Semantic color tokens */
@Immutable
data class FlowColorScheme(
    val layer0: Color,
    val layer1: Color,
    val layer2: Color,
    val layer3: Color,
    val textMain: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnAccent: Color,
    val iconMain: Color,
    val iconSecondary: Color,
    val primary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val divider: Color,
    val scrim: Color,
    val accents: Map<AccentColor, Color>,
    val accentSurfaces: Map<AccentColor, Color>,
    val isDark: Boolean,
) {
    fun accent(accent: AccentColor): Color = accents[accent] ?: primary

    fun accentSurface(accent: AccentColor): Color = accentSurfaces[accent] ?: primary.copy(alpha = SURFACE_ALPHA)

    private companion object {
        const val SURFACE_ALPHA = 0.16f
    }
}

private val DarkAccents: Map<AccentColor, Color> = mapOf(
    AccentColor.PURPLE to Color(0xFF8B7CFF),
    AccentColor.VIOLET to Color(0xFFA779FF),
    AccentColor.BLUE to Color(0xFF5AA9FF),
    AccentColor.TEAL to Color(0xFF4FD1C5),
    AccentColor.GREEN to Color(0xFF72D6A0),
    AccentColor.ORANGE to Color(0xFFF4A261),
    AccentColor.PINK to Color(0xFFEE6C9A),
)

private val LightAccents: Map<AccentColor, Color> = mapOf(
    AccentColor.PURPLE to Color(0xFF6957E8),
    AccentColor.VIOLET to Color(0xFF8B5CF6),
    AccentColor.BLUE to Color(0xFF2D7FF9),
    AccentColor.TEAL to Color(0xFF0F9E92),
    AccentColor.GREEN to Color(0xFF2FA46E),
    AccentColor.ORANGE to Color(0xFFE07C3E),
    AccentColor.PINK to Color(0xFFDB4F82),
)

/** Dark palette */
internal val DarkFlowColorScheme = FlowColorScheme(
    layer0 = Color(0xFF101116),
    layer1 = Color(0xFF181A22),
    layer2 = Color(0xFF21232E),
    layer3 = Color(0xFF2A2D3A),
    textMain = Color(0xFFF4F5F8),
    textSecondary = Color(0xFF9B9EAE),
    textTertiary = Color(0xFF6C7083),
    textOnAccent = Color(0xFFFFFFFF),
    iconMain = Color(0xFFF4F5F8),
    iconSecondary = Color(0xFF9B9EAE),
    primary = Color(0xFF8B7CFF),
    success = Color(0xFF72D6A0),
    warning = Color(0xFFF4C56A),
    error = Color(0xFFF2698B),
    divider = Color(0xFF262936),
    scrim = Color(0xCC0B0C10),
    accents = DarkAccents,
    accentSurfaces = DarkAccents.mapValues { (_, color) -> color.copy(alpha = 0.18f) },
    isDark = true,
)

/** Light palette */
internal val LightFlowColorScheme = FlowColorScheme(
    layer0 = Color(0xFFF7F7FA),
    layer1 = Color(0xFFFFFFFF),
    layer2 = Color(0xFFF1F1F6),
    layer3 = Color(0xFFE6E7EE),
    textMain = Color(0xFF16171D),
    textSecondary = Color(0xFF5E6273),
    textTertiary = Color(0xFF8E93A5),
    textOnAccent = Color(0xFFFFFFFF),
    iconMain = Color(0xFF16171D),
    iconSecondary = Color(0xFF5E6273),
    primary = Color(0xFF6957E8),
    success = Color(0xFF2FA46E),
    warning = Color(0xFFCF9420),
    error = Color(0xFFD6436A),
    divider = Color(0xFFE4E5EC),
    scrim = Color(0x99101116),
    accents = LightAccents,
    accentSurfaces = LightAccents.mapValues { (_, color) -> color.copy(alpha = 0.14f) },
    isDark = false,
)