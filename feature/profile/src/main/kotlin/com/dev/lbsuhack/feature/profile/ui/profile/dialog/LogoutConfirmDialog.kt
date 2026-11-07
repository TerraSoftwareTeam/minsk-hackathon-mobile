package com.dev.lbsuhack.feature.profile.ui.profile.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.dev.lbsuhack.core.ui.component.HackButtonContent
import com.dev.lbsuhack.feature.profile.R

@Composable
fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoggingOut: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = { if (!isLoggingOut) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.profile_logout_confirm_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Text(
                text = stringResource(R.string.profile_logout_confirm_desc),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = { if (!isLoggingOut) onConfirm() },
                shape = MaterialTheme.shapes.medium,
            ) {
                HackButtonContent(loading = isLoggingOut) {
                    Text(stringResource(R.string.profile_logout))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoggingOut,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Text(stringResource(R.string.profile_cancel))
            }
        },
    )
}
