package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackBadge
import com.dev.lbsuhack.feature.activities.R

@Composable
internal fun ActivityRatingSummary(score: Int, modifier: Modifier = Modifier) {
    HackBadge(text = stringResource(R.string.activities_rating_summary, score), modifier = modifier)
}
