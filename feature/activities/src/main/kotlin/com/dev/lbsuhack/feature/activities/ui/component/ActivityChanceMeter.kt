package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.domain.model.SuccessChance
import com.dev.lbsuhack.core.localization.R as LocalizationR
import com.dev.lbsuhack.core.localization.chance.hintRes
import com.dev.lbsuhack.core.localization.chance.labelRes
import com.dev.lbsuhack.core.ui.component.HackChanceMeter

@Composable
internal fun ActivityChanceMeter(
    probability: Double?,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
    title: String = stringResource(LocalizationR.string.chance_title),
) {
    val chance = probability?.let(::SuccessChance)
    HackChanceMeter(
        title = title,
        percent = chance?.percent,
        levelText = chance?.level?.let { stringResource(it.labelRes()) },
        modifier = modifier,
        prominent = prominent,
        description = if (prominent) chance?.level?.let { stringResource(it.hintRes()) } else null,
        footnote = if (prominent && chance != null) stringResource(LocalizationR.string.chance_basis) else null,
    )
}
