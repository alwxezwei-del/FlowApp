package ru.alexey.flowapp.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import ru.alexey.flowapp.core.ui.R

@Composable
fun habitProgressSubtitle(
    completed: Int,
    target: Int,
    streakDays: Int,
): String {
    val progress = stringResource(R.string.habit_progress, completed, target)
    if (streakDays <= 0) return progress
    val streak = pluralStringResource(R.plurals.habit_streak_days, streakDays, streakDays)
    return "$progress · $streak"
}