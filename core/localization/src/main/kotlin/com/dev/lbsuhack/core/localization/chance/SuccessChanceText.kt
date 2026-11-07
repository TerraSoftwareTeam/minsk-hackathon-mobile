package com.dev.lbsuhack.core.localization.chance

import androidx.annotation.StringRes
import com.dev.lbsuhack.core.domain.model.SuccessChanceLevel
import com.dev.lbsuhack.core.localization.R

@StringRes
fun SuccessChanceLevel.labelRes(): Int = when (this) {
    SuccessChanceLevel.LOW -> R.string.chance_level_low
    SuccessChanceLevel.MEDIUM -> R.string.chance_level_medium
    SuccessChanceLevel.HIGH -> R.string.chance_level_high
}

@StringRes
fun SuccessChanceLevel.hintRes(): Int = when (this) {
    SuccessChanceLevel.LOW -> R.string.chance_hint_low
    SuccessChanceLevel.MEDIUM -> R.string.chance_hint_medium
    SuccessChanceLevel.HIGH -> R.string.chance_hint_high
}
