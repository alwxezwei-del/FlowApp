package ru.alexey.flowapp.feature.focus.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alexey.flowapp.core.ui.CollectUiEvents
import ru.alexey.flowapp.core.ui.navigation.FlowRoute
import ru.alexey.flowapp.feature.focus.FocusScreen
import ru.alexey.flowapp.feature.focus.FocusUiEvent
import ru.alexey.flowapp.feature.focus.FocusViewModel
import ru.alexey.flowapp.feature.focus.service.FocusTimerService

fun NavGraphBuilder.focusScreen() {
    composable<FlowRoute.Focus> { entry ->
        val route = entry.toRoute<FlowRoute.Focus>()
        val viewModel: FocusViewModel = koinViewModel { parametersOf(route.taskId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val context = LocalContext.current

        val notificationPermission = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        CollectUiEvents<FocusUiEvent>(viewModel.events) { event ->
            when (event) {
                FocusUiEvent.StartService -> FocusTimerService.start(context)
                FocusUiEvent.StopService -> FocusTimerService.stop(context)
            }
        }

        FocusScreen(state = state, onAction = viewModel::onAction)
    }
}