package ru.alexey.flowapp.feature.settings.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alexey.flowapp.core.ui.CollectUiEvents
import ru.alexey.flowapp.core.ui.navigation.FlowRoute
import ru.alexey.flowapp.feature.settings.R
import ru.alexey.flowapp.feature.settings.SettingsScreen
import ru.alexey.flowapp.feature.settings.SettingsUiAction
import ru.alexey.flowapp.feature.settings.SettingsUiEvent
import ru.alexey.flowapp.feature.settings.SettingsViewModel

/**
 * Registers the settings screen
 */
fun NavGraphBuilder.settingsScreen(
    appVersion: String,
    onBack: () -> Unit,
) {
    composable<FlowRoute.Settings> {
        val viewModel: SettingsViewModel = koinViewModel { parametersOf(appVersion) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val context = LocalContext.current

        var pendingExport by remember { mutableStateOf<String?>(null) }
        val exportFileName = stringResource(R.string.settings_export_file_name)
        val exportSuccess = stringResource(R.string.settings_export_success)
        val exportFailed = stringResource(R.string.settings_export_failed)
        val resources = LocalResources.current
        val importFailed = stringResource(R.string.settings_import_failed)

        val createFile = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(JSON_MIME_TYPE),
        ) { uri ->
            val content = pendingExport
            pendingExport = null
            if (uri == null || content == null) return@rememberLauncherForActivityResult

            val written = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
            }.isSuccess
            Toast.makeText(context, if (written) exportSuccess else exportFailed, Toast.LENGTH_SHORT).show()
        }

        val openFile = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val content = runCatching {
                context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
            }.getOrNull()

            if (content == null) {
                Toast.makeText(context, importFailed, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.onAction(SettingsUiAction.ImportFileRead(content))
            }
        }

        CollectUiEvents<SettingsUiEvent>(viewModel.events, onNavigateUp = onBack) { event ->
            when (event) {
                is SettingsUiEvent.ExportReady -> {
                    pendingExport = event.json
                    createFile.launch(exportFileName)
                }

                is SettingsUiEvent.Message -> {
                    val message = resources.getString(event.messageRes, *event.formatArgs.toTypedArray())
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }

        SettingsScreen(
            state = state,
            onAction = viewModel::onAction,
            onExport = { viewModel.onAction(SettingsUiAction.ExportRequested) },
            onImport = { openFile.launch(arrayOf(JSON_MIME_TYPE, ANY_MIME_TYPE)) },
            onBack = onBack,
        )
    }
}

private const val JSON_MIME_TYPE = "application/json"

private const val ANY_MIME_TYPE = "application/octet-stream"