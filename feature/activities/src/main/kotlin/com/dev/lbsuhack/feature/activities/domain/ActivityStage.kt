package com.dev.lbsuhack.feature.activities.domain

enum class ActivityStage {
    VOTING,
    WAITING,
    CONFIRMED,
    CLOSED,
}

val ActivityStatus.stage: ActivityStage
    get() = when (this) {
        ActivityStatus.ON_VOTE -> ActivityStage.VOTING
        ActivityStatus.ON_MODERATION,
        ActivityStatus.ON_ADMIN_MODERATION,
        -> ActivityStage.WAITING
        ActivityStatus.FINALIZED -> ActivityStage.CONFIRMED
        ActivityStatus.DECLINED,
        ActivityStatus.DECLINED_BY_ADMIN,
        ActivityStatus.EXPIRED,
        -> ActivityStage.CLOSED
    }

val ActivityProposal.stage: ActivityStage
    get() = status.stage
