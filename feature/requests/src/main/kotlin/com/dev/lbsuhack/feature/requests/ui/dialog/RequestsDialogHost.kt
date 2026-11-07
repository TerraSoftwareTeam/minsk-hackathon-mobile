package com.dev.lbsuhack.feature.requests.ui.dialog

import androidx.compose.runtime.Composable
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.store.requests.RequestDialog

@Composable
internal fun RequestsDialogHost(
    dialog: RequestDialog?,
    audience: RequestAudience?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onChangeReason: (String) -> Unit,
    onConfirmApprove: () -> Unit,
    onConfirmDecline: () -> Unit,
) {
    when (dialog) {
        null -> Unit

        is RequestDialog.Approve -> ApproveRequestDialog(
            dialog = dialog,
            audience = audience,
            isSubmitting = isSubmitting,
            onDismiss = onDismiss,
            onConfirm = onConfirmApprove,
        )

        is RequestDialog.Decline -> DeclineRequestDialog(
            dialog = dialog,
            isSubmitting = isSubmitting,
            onDismiss = onDismiss,
            onChangeReason = onChangeReason,
            onConfirm = onConfirmDecline,
        )
    }
}
