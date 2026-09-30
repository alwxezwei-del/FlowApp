package ru.alexey.flowapp.core.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/** Immutable screen state */
@Stable
interface UiState

/** User action from composable to ViewModel */
interface UiAction

/** Event: navigation, toast, close */
interface UiEvent

/** Events shared by all screens */
sealed interface DefaultUiEvent : UiEvent {
    /** Show a short message */
    data class Message(
        val message: String,
        val isError: Boolean = false,
    ) : DefaultUiEvent

    /** Navigate back */
    data object NavigateUp : DefaultUiEvent
}

/**
 * Base screen ViewModel (UDF): [state] down, [onAction]
 */
abstract class BaseComposeViewModel<S : UiState, A : UiAction>(
    initialState: S,
) : ViewModel(),
    EventHolder by DefaultEventHolder() {
    protected open val _state: MutableStateFlow<S> = MutableStateFlow(initialState)

    open val state: StateFlow<S> get() = _state.asStateFlow()

    abstract fun onAction(action: A)
}

/** Events source */
interface EventHolder {
    val events: Flow<UiEvent>

    suspend fun sendEvent(event: UiEvent)
}

/**
 * [EventHolder] backed by [MutableSharedFlow].
 *
 */
class DefaultEventHolder(
    extraBufferCapacity: Int = 1,
) : EventHolder {
    private val _events = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = extraBufferCapacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val events: Flow<UiEvent> = _events.asSharedFlow()

    override suspend fun sendEvent(event: UiEvent) {
        _events.emit(event)
    }
}