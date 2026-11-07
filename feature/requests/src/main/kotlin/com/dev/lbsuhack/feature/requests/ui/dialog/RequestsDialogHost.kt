package com.dev.lbsuhack.feature.requests.ui.dialog

import androidx.compose.runtime.Composable
import com.dev.lbsuhack.feature.requests.store.requests.RequestDialog

@Composable
internal fun RequestsDialogHost(
    dialog: RequestDialog?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onChangeReason: (String) -> Unit,
    onConfirmDecline: () -> Unit,
) {
    when (dialog) {
        null -> Unit

        is RequestDialog.Decline -> DeclineRequestDialog(
            dialog = dialog,
            isSubmitting = isSubmitting,
            onDismiss = onDismiss,
            onChangeReason = onChangeReason,
            onConfirm = onConfirmDecline,
        )
    }
}
