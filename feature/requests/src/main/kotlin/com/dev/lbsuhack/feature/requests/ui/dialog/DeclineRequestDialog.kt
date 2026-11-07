package com.dev.lbsuhack.feature.requests.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.MAX_DECLINE_REASON_LENGTH
import com.dev.lbsuhack.feature.requests.store.requests.RequestDialog

@Composable
internal fun DeclineRequestDialog(
    dialog: RequestDialog.Decline,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onChangeReason: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Text(
                text = stringResource(R.string.requests_dialog_decline_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = dialog.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.requests_dialog_decline_text),
                    style = MaterialTheme.typography.bodyMedium,
                )
                DialogTextField(
                    value = dialog.reason,
                    onValueChange = onChangeReason,
                    label = stringResource(R.string.requests_dialog_decline_reason_label),
                    hint = stringResource(R.string.requests_dialog_decline_reason_hint),
                    minLines = 2,
                    enabled = !isSubmitting,
                    supportingText = stringResource(
                        R.string.requests_reason_counter,
                        dialog.reason.length,
                        MAX_DECLINE_REASON_LENGTH,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onError,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.requests_action_decline),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
            ) {
                Text(stringResource(R.string.requests_action_cancel))
            }
        },
    )
}
