package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackDetailsLink
import com.dev.lbsuhack.feature.activities.R

@Composable
internal fun ActivityDetailsLink(modifier: Modifier = Modifier) {
    HackDetailsLink(text = stringResource(R.string.activities_open_details), modifier = modifier)
}
