package com.dev.lbsuhack.feature.activities.ui.create.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.ui.component.HackInfoButton
import com.dev.lbsuhack.feature.activities.R

@Composable
internal fun MaxParticipantsField(
    maxParticipants: Int?,
    minParticipants: Int,
    maxAllowed: Int,
    enabled: Boolean,
    onChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val limited = maxParticipants != null
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .toggleable(
                    value = limited,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = { checked -> onChange(if (checked) minParticipants * 2 else null) },
                )
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.create_activity_max_label),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            HackInfoButton(
                title = stringResource(R.string.create_activity_max_label),
                text = stringResource(R.string.create_activity_max_hint),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = limited,
                onCheckedChange = null,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                    checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
        AnimatedVisibility(visible = maxParticipants != null) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                val value = maxParticipants ?: minParticipants
                ParticipantCountRow(
                    title = stringResource(R.string.create_activity_max_count_label),
                    value = value,
                    decrementEnabled = enabled && value > minParticipants,
                    incrementEnabled = enabled && value < maxAllowed,
                    onDecrement = { onChange(value - 1) },
                    onIncrement = { onChange(value + 1) },
                )
            }
        }
    }
}
