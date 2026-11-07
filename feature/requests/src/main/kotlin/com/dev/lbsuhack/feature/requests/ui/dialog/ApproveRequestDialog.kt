package com.dev.lbsuhack.feature.requests.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.localization.R as LocalizationR
import com.dev.lbsuhack.core.ui.component.HackButtonContent
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.store.requests.RequestDialog
import com.dev.lbsuhack.feature.requests.ui.component.RequestChanceMeter
import com.dev.lbsuhack.feature.requests.ui.component.RequestDateTimeField
import java.time.OffsetDateTime

@Composable
internal fun ApproveRequestDialog(
    dialog: RequestDialog.Approve,
    audience: RequestAudience?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onChangeStartsAt: (OffsetDateTime) -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Text(
                text = stringResource(R.string.requests_dialog_approve_title),
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
                    text = stringResource(
                        if (audience == RequestAudience.ADMIN) {
                            R.string.requests_dialog_approve_admin_text
                        } else {
                            R.string.requests_dialog_approve_teacher_text
                        },
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                val startsAt = dialog.startsAt
                if (startsAt != null) {
                    Text(
                        text = stringResource(R.string.requests_dialog_approve_date_label),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                    RequestDateTimeField(
                        startsAt = startsAt,
                        enabled = !isSubmitting,
                        onChange = onChangeStartsAt,
                    )
                    if (dialog.hasDateError) {
                        Text(
                            text = stringResource(R.string.requests_error_date_in_past),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                if (dialog.isChanceLoading || dialog.chance != null) {
                    RequestChanceMeter(
                        probability = dialog.chance.takeUnless { dialog.isChanceLoading },
                        title = stringResource(
                            if (startsAt != null) LocalizationR.string.chance_for_date else LocalizationR.string.chance_title,
                        ),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isSubmitting) onConfirm() },
                shape = MaterialTheme.shapes.medium,
            ) {
                HackButtonContent(loading = isSubmitting) {
                    Text(
                        text = stringResource(R.string.requests_action_approve),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                onClick = onDismiss,
                enabled = !isSubmitting,
            ) {
                Text(stringResource(R.string.requests_action_cancel))
            }
        },
    )
}
