package com.dev.lbsuhack.feature.profile.ui.profile.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.ui.component.HackButtonContent
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.store.profile.ProfileEditDialog

@Composable
fun EditProfileDialog(
    dialog: ProfileEditDialog,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onChangeName: (String) -> Unit,
    onChangeSurname: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.profile_edit_dialog_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EditTextField(
                    value = dialog.name,
                    onValueChange = onChangeName,
                    label = stringResource(R.string.profile_edit_name_label),
                    hint = stringResource(R.string.profile_edit_name_hint),
                    imeAction = ImeAction.Next,
                )
                EditTextField(
                    value = dialog.surname,
                    onValueChange = onChangeSurname,
                    label = stringResource(R.string.profile_edit_surname_label),
                    hint = stringResource(R.string.profile_edit_surname_hint),
                    imeAction = ImeAction.Done,
                    onImeAction = onConfirm,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isSaving) onConfirm() },
                shape = MaterialTheme.shapes.medium,
            ) {
                HackButtonContent(loading = isSaving) {
                    Text(stringResource(R.string.profile_edit_save))
                }
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                onClick = onDismiss,
                enabled = !isSaving,
            ) {
                Text(stringResource(R.string.profile_cancel))
            }
        },
    )
}
