package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackSectionHeader
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityStage

@Composable
internal fun ActivitiesSectionHeader(stage: ActivityStage, count: Int, modifier: Modifier = Modifier) {
    HackSectionHeader(
        title = stringResource(stage.sectionTitleRes()),
        count = count,
        modifier = modifier,
    )
}

private fun ActivityStage.sectionTitleRes(): Int = when (this) {
    ActivityStage.VOTING -> R.string.activities_section_voting
    ActivityStage.CONFIRMED -> R.string.activities_section_confirmed
    ActivityStage.WAITING -> R.string.activities_section_waiting
    ActivityStage.CLOSED -> R.string.activities_section_closed
}
