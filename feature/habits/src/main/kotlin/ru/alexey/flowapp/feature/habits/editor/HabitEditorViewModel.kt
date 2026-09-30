package ru.alexey.flowapp.feature.habits.editor

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.HabitRepository
import ru.alexey.flowapp.core.model.Habit
import ru.alexey.flowapp.core.model.HabitSchedule
import ru.alexey.flowapp.core.ui.BaseComposeViewModel

@KoinViewModel
internal class HabitEditorViewModel(
    @InjectedParam private val habitId: String?,
    private val habitRepository: HabitRepository,
    private val timeProvider: TimeProvider,
) : BaseComposeViewModel<HabitEditorUiState, HabitEditorUiAction>(
        initialState = HabitEditorUiState(isEditing = habitId != null),
    ) {
    init {
        load()
    }

    override fun onAction(action: HabitEditorUiAction) {
        when (action) {
            is HabitEditorUiAction.NameChanged -> _state.update { it.copy(name = action.value) }
            is HabitEditorUiAction.IconSelected -> _state.update { it.copy(icon = action.icon) }
            is HabitEditorUiAction.ColorSelected -> _state.update { it.copy(color = action.color) }
            is HabitEditorUiAction.ScheduleChanged -> _state.update { it.copy(daily = action.daily) }
            is HabitEditorUiAction.DayToggled -> toggleDay(action)
            is HabitEditorUiAction.TargetChanged -> changeTarget(action.delta)
            is HabitEditorUiAction.ReminderChanged -> _state.update { it.copy(reminderMinuteOfDay = action.minuteOfDay) }
            is HabitEditorUiAction.NoteChanged -> _state.update { it.copy(note = action.value) }
            HabitEditorUiAction.Save -> save()
            HabitEditorUiAction.Delete -> delete()
        }
    }

    private fun toggleDay(action: HabitEditorUiAction.DayToggled) {
        _state.update { current ->
            val days = current.selectedDays.toMutableSet()
            if (!days.add(action.day)) days.remove(action.day)
            current.copy(selectedDays = days)
        }
    }

    private fun changeTarget(delta: Int) {
        _state.update { it.copy(targetPerDay = (it.targetPerDay + delta).coerceIn(MIN_TARGET, MAX_TARGET)) }
    }

    private fun load() {
        viewModelScope.launch {
            val habit = habitId?.let { id -> habitRepository.observeHabit(id).first() }
            _state.update { current ->
                if (habit == null) {
                    current.copy(isLoading = false)
                } else {
                    val schedule = habit.schedule
                    current.copy(
                        isLoading = false,
                        name = habit.name,
                        icon = habit.icon,
                        color = habit.color,
                        daily = schedule is HabitSchedule.Daily,
                        selectedDays = (schedule as? HabitSchedule.SelectedDays)?.days ?: current.selectedDays,
                        targetPerDay = habit.targetPerDay,
                        reminderMinuteOfDay = habit.reminderMinuteOfDay,
                        note = habit.note.orEmpty(),
                    )
                }
            }
        }
    }

    private fun save() {
        val current = state.value
        if (!current.canSave) return

        viewModelScope.launch {
            val existing = habitId?.let { habitRepository.observeHabit(it).first() }
            val schedule = if (current.daily) {
                HabitSchedule.Daily
            } else {
                HabitSchedule.SelectedDays(current.selectedDays)
            }

            val habit = existing?.copy(
                name = current.name.trim(),
                icon = current.icon,
                color = current.color,
                schedule = schedule,
                targetPerDay = current.targetPerDay,
                reminderMinuteOfDay = current.reminderMinuteOfDay,
                note = current.note.trim().takeIf { it.isNotEmpty() },
            ) ?: Habit(
                name = current.name.trim(),
                icon = current.icon,
                color = current.color,
                schedule = schedule,
                targetPerDay = current.targetPerDay,
                reminderMinuteOfDay = current.reminderMinuteOfDay,
                note = current.note.trim().takeIf { it.isNotEmpty() },
                createdAt = timeProvider.now(),
            )

            if (existing == null) habitRepository.createHabit(habit) else habitRepository.updateHabit(habit)
            sendEvent(HabitEditorUiEvent.Close)
        }
    }

    private fun delete() {
        val id = habitId ?: return
        viewModelScope.launch {
            habitRepository.deleteHabit(id)
            sendEvent(HabitEditorUiEvent.Close)
        }
    }

    private companion object {
        const val MIN_TARGET = 1
        const val MAX_TARGET = 20
    }
}