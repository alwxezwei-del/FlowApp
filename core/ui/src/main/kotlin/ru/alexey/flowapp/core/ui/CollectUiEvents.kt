package ru.alexey.flowapp.core.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collects screen events
 */
@Composable
inline fun <reified T : UiEvent> CollectUiEvents(
    events: Flow<UiEvent>,
    noinline onNavigateUp: () -> Unit = {},
    crossinline block: suspend (T) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            events.collect { event ->
                when (event) {
                    is DefaultUiEvent.Message -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }

                    is DefaultUiEvent.NavigateUp -> {
                        onNavigateUp()
                    }

                    is T -> {
                        block(event)
                    }

                    else -> {
                        Unit
                    }
                }
            }
        }
    }
}