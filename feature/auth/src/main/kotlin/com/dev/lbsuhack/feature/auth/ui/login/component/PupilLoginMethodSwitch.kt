package com.dev.lbsuhack.feature.auth.ui.login.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.store.login.PupilLoginMethod

@Composable
internal fun PupilLoginMethodSwitch(
    selected: PupilLoginMethod,
    onSelect: (PupilLoginMethod) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val methods = PupilLoginMethod.entries
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(SWITCH_PADDING),
        ) {
            val segmentWidth: Dp = maxWidth / methods.size
            val indicatorOffset by animateDpAsState(
                targetValue = segmentWidth * methods.indexOf(selected),
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
                label = "PupilLoginMethodIndicator",
            )
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                    ),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .selectableGroup(),
            ) {
                methods.forEach { method ->
                    MethodSegment(
                        label = stringResource(method.labelRes()),
                        icon = method.icon(),
                        isSelected = method == selected,
                        enabled = enabled,
                        onClick = { onSelect(method) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MethodSegment(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "PupilLoginMethodContent",
    )
    Row(
        modifier = modifier
            .semantics {
                selected = isSelected
                role = Role.Tab
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                enabled = enabled && !isSelected,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

private fun PupilLoginMethod.labelRes(): Int = when (this) {
    PupilLoginMethod.SCHOOLS_BY -> R.string.auth_pupil_method_schools_by
    PupilLoginMethod.MANUAL -> R.string.auth_pupil_method_manual
}

private fun PupilLoginMethod.icon(): ImageVector = when (this) {
    PupilLoginMethod.SCHOOLS_BY -> Icons.Rounded.School
    PupilLoginMethod.MANUAL -> Icons.Rounded.EditNote
}

private val SWITCH_PADDING = 4.dp
