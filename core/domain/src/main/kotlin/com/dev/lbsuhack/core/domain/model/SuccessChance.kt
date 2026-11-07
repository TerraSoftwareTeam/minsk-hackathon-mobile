package com.dev.lbsuhack.core.domain.model

import kotlin.math.roundToInt

enum class SuccessChanceLevel {
    LOW,
    MEDIUM,
    HIGH,
}

@JvmInline
value class SuccessChance(val probability: Double) {

    val percent: Int
        get() = (probability.coerceIn(0.0, 1.0) * PERCENT).roundToInt()

    val level: SuccessChanceLevel
        get() = when {
            probability >= HIGH_THRESHOLD -> SuccessChanceLevel.HIGH
            probability >= MEDIUM_THRESHOLD -> SuccessChanceLevel.MEDIUM
            else -> SuccessChanceLevel.LOW
        }

    private companion object {
        const val PERCENT = 100
        const val HIGH_THRESHOLD = 0.7
        const val MEDIUM_THRESHOLD = 0.45
    }
}
