package ru.alexey.flowapp.feature.history

import androidx.compose.runtime.Immutable
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.HistoryFilter
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiState

enum class HistoryEventKind {
    FOCUS,
    TASK,
    HABIT,
}

/**
 * Feed event
 */
@Immutable
data class HistoryEventUi(
    val id: String,
    val time: String,
    val title: String,
    val subtitle: String?,
    val kind: HistoryEventKind,
    val icon: String,
    val accent: AccentColor,
)

/**
 * Events of one day
 */
@Immutable
data class HistoryDayUi(
    val title: String,
    val events: List<HistoryEventUi>,
)

/** History feed state */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val filter: HistoryFilter = HistoryFilter.Default,
    val days: List<HistoryDayUi> = emptyList(),
) : UiState

/** History feed actions */
sealed interface HistoryUiAction : UiAction {
    data class SelectFilter(
        val filter: HistoryFilter,
    ) : HistoryUiAction
}