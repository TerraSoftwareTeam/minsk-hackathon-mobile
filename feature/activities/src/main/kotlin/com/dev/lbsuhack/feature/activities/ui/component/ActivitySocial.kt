package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R

@Composable
internal fun ActivityRating(
    likesCount: Int,
    likedByMe: Boolean,
    enabled: Boolean,
    onLike: () -> Unit,
    onUnlike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    Row(
        modifier = modifier
            .background(
                if (likedByMe) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
                shape,
            )
            .border(
                width = 1.dp,
                color = if (likedByMe) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RatingArrow(
            up = true,
            active = likedByMe,
            enabled = enabled,
            onClick = { if (likedByMe) onUnlike() else onLike() },
        )
        AnimatedContent(
            targetState = likesCount,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInVertically(tween(180)) { it * direction } + fadeIn(tween(180))) togetherWith
                    (slideOutVertically(tween(180)) { -it * direction } + fadeOut(tween(180)))
            },
            label = "rating",
        ) { count ->
            Text(
                text = count.toString(),
                modifier = Modifier.widthIn(min = 20.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        RatingArrow(
            up = false,
            active = false,
            enabled = enabled && likedByMe,
            onClick = onUnlike,
        )
    }
}

@Composable
private fun RatingArrow(up: Boolean, active: Boolean, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        ),
    ) {
        Icon(
            imageVector = if (up) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
            contentDescription = stringResource(if (up) R.string.activities_rating_up else R.string.activities_rating_down),
            modifier = Modifier.size(if (active) 28.dp else 24.dp),
        )
    }
}

@Composable
internal fun ActivityVotersSummary(voters: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (voters.isNotEmpty()) {
            VoterAvatarStack(voters = voters)
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = votersText(voters),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun ActivityVotersList(voters: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        voters.forEach { voter ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                VoterAvatar(name = voter, size = 36.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = voter,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun VoterAvatarStack(voters: List<String>) {
    val shown = voters.take(MAX_STACK)
    val extra = voters.size - shown.size
    val size = 26.dp
    val overlap = 8.dp
    val count = shown.size + if (extra > 0) 1 else 0
    Box(modifier = Modifier.width(size + (size - overlap) * (count - 1))) {
        shown.forEachIndexed { index, voter ->
            VoterAvatar(name = voter, size = size, modifier = Modifier.offset(x = (size - overlap) * index))
        }
        if (extra > 0) {
            VoterBubble(text = "+$extra", size = size, modifier = Modifier.offset(x = (size - overlap) * shown.size))
        }
    }
}

@Composable
private fun VoterAvatar(name: String, size: Dp, modifier: Modifier = Modifier) {
    VoterBubble(text = initials(name), size = size, modifier = modifier)
}

@Composable
private fun VoterBubble(text: String, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.surfaceContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = if (size < 32.dp) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun votersText(voters: List<String>): String {
    val names = voters.map(::shortName)
    return when (names.size) {
        0 -> stringResource(R.string.activities_voters_empty)
        1 -> stringResource(R.string.activities_voters_one, names[0])
        2 -> stringResource(R.string.activities_voters_two, names[0], names[1])
        else -> stringResource(R.string.activities_voters_many, names[0], names[1], names.size - 2)
    }
}

internal fun initials(name: String): String =
    name.split(' ').filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

internal fun shortName(name: String): String {
    val parts = name.split(' ').filter(String::isNotBlank)
    return when {
        parts.isEmpty() -> name
        parts.size == 1 -> parts[0]
        else -> "${parts[0]} ${parts[1].first().uppercaseChar()}."
    }
}

private const val MAX_STACK = 4

@Composable
internal fun ActivityVoteButton(
    proposal: com.dev.lbsuhack.feature.activities.domain.ActivityProposal,
    isAuthor: Boolean,
    onVote: () -> Unit,
    onUnvote: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDetails: (() -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.medium
    when {
        proposal.hasVoted -> {
            val canCancel = proposal.canChangeVote && !isAuthor
            androidx.compose.material3.OutlinedButton(
                onClick = onUnvote,
                enabled = canCancel,
                modifier = modifier,
                shape = shape,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (canCancel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                ),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.activities_leave_action),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        proposal.canChangeVote -> {
            androidx.compose.material3.Button(
                onClick = onVote,
                modifier = modifier,
                shape = shape,
            ) {
                Text(
                    text = stringResource(R.string.activities_join_action),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        onOpenDetails != null -> {
            androidx.compose.material3.OutlinedButton(
                onClick = onOpenDetails,
                modifier = modifier,
                shape = shape,
            ) {
                Text(
                    text = stringResource(R.string.activities_open_details),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}
