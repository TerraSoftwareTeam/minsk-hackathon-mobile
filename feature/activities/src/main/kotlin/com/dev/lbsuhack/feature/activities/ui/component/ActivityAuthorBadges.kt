package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityOrigin
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal

@Composable
internal fun ActivityAuthorBadges(proposal: ActivityProposal, isAuthor: Boolean) {
    val originRes = when (proposal.origin) {
        ActivityOrigin.PUPIL -> null
        ActivityOrigin.TEACHER -> R.string.activity_origin_teacher
        ActivityOrigin.SCHOOL -> R.string.activity_origin_school
    }
    if (originRes != null) {
        Spacer(modifier = Modifier.width(8.dp))
        ActivityAuthorBadge(text = stringResource(originRes))
    }
    if (isAuthor) {
        Spacer(modifier = Modifier.width(6.dp))
        ActivityAuthorBadge(text = stringResource(R.string.activity_author_you))
    }
}
