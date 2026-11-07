package com.dev.lbsuhack.feature.activities.util

import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus

object TestFixtures {

    val pupil: SessionUser = SessionUser(
        id = "101",
        name = "Никита Ковалев",
        schoolsById = "schools-101",
        schoolName = "School #1",
        pupilClass = "10-А",
    )

    val ownProposal: ActivityProposal = ActivityProposal(
        id = "act-1",
        title = "Футбольный матч",
        description = "Игра на поле",
        category = ActivityCategory.SPORTS,
        scope = ActivityScope.SCHOOL,
        status = ActivityStatus.ON_VOTE,
        voteThreshold = 15,
        votesCount = 10,
        hasVoted = false,
        startsAt = "2026-11-21T10:00:00.000Z",
        votingClosesAt = "2026-11-19T10:00:00.000Z",
        authorName = "Никита Ковалев",
        isMine = true,
    )

    val otherProposal: ActivityProposal = ActivityProposal(
        id = "act-2",
        title = "Шахматный турнир",
        description = "Быстрые шахматы",
        category = ActivityCategory.INTELLECTUAL,
        scope = ActivityScope.GRADE,
        status = ActivityStatus.ON_VOTE,
        voteThreshold = 10,
        votesCount = 10,
        hasVoted = false,
        startsAt = "2026-11-21T12:00:00.000Z",
        votingClosesAt = "2026-11-19T12:00:00.000Z",
        authorName = "Мария",
        isMine = false,
    )
}
