package ru.alexey.flowapp.feature.focus.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import ru.alexey.flowapp.core.designsystem.component.FlowTextField
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.feature.focus.R

/** Custom interval duration dialog */
@Composable
internal fun CustomDurationDialog(
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialMinutes.toString()) }
    val minutes = text.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FlowTheme.colors.layer1,
        title = {
            Text(
                text = stringResource(R.string.focus_custom_title),
                style = FlowTheme.typography.title2,
                color = FlowTheme.colors.textMain,
            )
        },
        text = {
            FlowTextField(
                label = stringResource(R.string.focus_custom_field),
                value = text,
                onValueChange = { input -> text = input.filter(Char::isDigit).take(3) },
                keyboardType = KeyboardType.Number,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { minutes?.let(onConfirm) },
                enabled = minutes != null && minutes > 0,
            ) {
                Text(text = stringResource(R.string.focus_custom_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(ru.alexey.flowapp.core.ui.R.string.action_cancel))
            }
        },
    )
}