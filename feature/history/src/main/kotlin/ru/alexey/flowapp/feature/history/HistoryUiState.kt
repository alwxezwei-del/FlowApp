package ru.alexey.flowapp.feature.history

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.HistoryFilter
import ru.alexey.flowapp.core.ui.UiAction
import ru.alexey.flowapp.core.ui.UiState

sealed interface HistoryEventContent {
    /**
     * @param duration formatted actual duration
     * @param taskTitle null for a free session
     */
    data class Focus(
        val duration: String,
        val kind: FocusKind,
        val taskTitle: String?,
        val interrupted: Boolean,
    ) : HistoryEventContent

    data class Task(
        val title: String,
        val categoryName: String?,
    ) : HistoryEventContent

    data class Habit(
        val name: String,
    ) : HistoryEventContent
}

/**
 * Feed event
 */
@Immutable
data class HistoryEventUi(
    val id: String,
    val time: String,
    val icon: String,
    val accent: AccentColor,
    val content: HistoryEventContent,
)

/** Day shown by name instead of date */
enum class RelativeDay {
    TODAY,
    YESTERDAY,
}

/**
 * Events of one day
 *
 * @param relative set for today and yesterday
 */
@Immutable
data class HistoryDayUi(
    val date: LocalDate,
    val relative: RelativeDay?,
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