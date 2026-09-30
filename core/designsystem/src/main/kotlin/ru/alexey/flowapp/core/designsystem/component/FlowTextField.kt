package ru.alexey.flowapp.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme

/** Text field with a static label above it */
@Composable
fun FlowTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val colors = FlowTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FlowTheme.spacers.x6),
    ) {
        Text(text = label, style = FlowTheme.typography.body2, color = colors.textSecondary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            textStyle = FlowTheme.typography.body1,
            shape = RoundedCornerShape(FlowTheme.radius.x12),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = placeholder?.let {
                {
                    Text(text = it, style = FlowTheme.typography.body1, color = colors.textTertiary)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.layer2,
                unfocusedContainerColor = colors.layer2,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.divider,
                focusedTextColor = colors.textMain,
                unfocusedTextColor = colors.textMain,
                cursorColor = colors.primary,
            ),
        )
    }
}