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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun ActivityRating(
    likesCount: Int,
    likedByMe: Boolean,
    dislikedByMe: Boolean,
    enabled: Boolean,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onUnlike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    Row(
        modifier = modifier
            .background(
                if (likedByMe || dislikedByMe) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
                shape,
            )
            .border(
                width = 1.dp,
                color = if (likedByMe || dislikedByMe) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
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
            active = dislikedByMe,
            enabled = enabled,
            onClick = { if (dislikedByMe) onUnlike() else onDislike() },
        )
    }
}
