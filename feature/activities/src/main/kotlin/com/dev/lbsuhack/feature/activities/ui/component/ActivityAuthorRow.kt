package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackInfoButton
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityOrigin
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal

@Composable
internal fun ActivityAuthorRow(proposal: ActivityProposal, isAuthor: Boolean) {
    val isSchool = proposal.origin == ActivityOrigin.SCHOOL
    val name = if (isSchool) stringResource(R.string.activity_origin_school) else proposal.authorName
    val role = when (proposal.origin) {
        ActivityOrigin.SCHOOL -> null
        ActivityOrigin.TEACHER -> stringResource(R.string.activity_origin_teacher)
        ActivityOrigin.PUPIL -> stringResource(R.string.activity_origin_pupil)
    }
    val moderator = proposal.teacherName?.takeIf { it.isNotBlank() }
        ?.let { stringResource(R.string.activity_organizer_moderator, it) }
    val info = listOfNotNull(
        role,
        if (isAuthor && !isSchool) stringResource(R.string.activity_author_you) else null,
        moderator,
    ).joinToString("\n\n")
    HackInfoButton(
        title = name,
        text = info,
        buttonText = stringResource(R.string.activity_show_organizer),
        icon = if (isSchool) Icons.Outlined.AccountBalance else Icons.Outlined.Person,
    )
}
