package com.dev.lbsuhack.feature.activities.presentation

import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal

enum class ActivityRatingMode {
    HIDDEN,
    INTERACTIVE,
    SUMMARY,
}

data class ActivityActionsPresentation(
    val ratingMode: ActivityRatingMode,
    val showParticipation: Boolean,
) {
    val isVisible: Boolean
        get() = showParticipation || ratingMode != ActivityRatingMode.HIDDEN
}

fun ActivityProposal.actionsFor(viewerRole: UserRole?): ActivityActionsPresentation {
    val ratingMode = when {
        !canLike -> ActivityRatingMode.HIDDEN
        viewerRole == UserRole.PUPIL -> ActivityRatingMode.INTERACTIVE
        (viewerRole == UserRole.TEACHER || viewerRole == UserRole.ORGANIZATOR) && likesCount != 0 -> ActivityRatingMode.SUMMARY
        else -> ActivityRatingMode.HIDDEN
    }
    return ActivityActionsPresentation(ratingMode, showParticipation = viewerRole == UserRole.PUPIL)
}
