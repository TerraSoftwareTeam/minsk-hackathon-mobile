package com.dev.lbsuhack.feature.activities.domain

import com.dev.lbsuhack.core.domain.model.SuccessChance
import com.dev.lbsuhack.core.domain.model.SuccessChanceLevel
import com.dev.lbsuhack.feature.activities.util.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SuccessChanceTest {

    @Test
    fun percentIsRoundedAndClamped() {
        assertEquals(58, SuccessChance(0.58).percent)
        assertEquals(55, SuccessChance(0.5461).percent)
        assertEquals(100, SuccessChance(1.4).percent)
        assertEquals(0, SuccessChance(-0.2).percent)
    }

    @Test
    fun levelsFollowTheThresholds() {
        assertEquals(SuccessChanceLevel.LOW, SuccessChance(0.3).level)
        assertEquals(SuccessChanceLevel.MEDIUM, SuccessChance(0.5).level)
        assertEquals(SuccessChanceLevel.MEDIUM, SuccessChance(0.58).level)
        assertEquals(SuccessChanceLevel.HIGH, SuccessChance(0.9).level)
    }

    @Test
    fun chanceIsShownOnlyWhileTheEventIsUndecided() {
        val base = TestFixtures.otherProposal.copy(successProbability = 0.58)
        assertTrue(base.copy(status = ActivityStatus.ON_VOTE).showsChance)
        assertTrue(base.copy(status = ActivityStatus.ON_MODERATION).showsChance)
        assertTrue(base.copy(status = ActivityStatus.ON_ADMIN_MODERATION).showsChance)
        assertFalse(base.copy(status = ActivityStatus.FINALIZED).showsChance)
        assertFalse(base.copy(status = ActivityStatus.DECLINED).showsChance)
        assertFalse(base.copy(status = ActivityStatus.EXPIRED).showsChance)
        assertFalse(base.copy(status = ActivityStatus.ON_VOTE, successProbability = null).showsChance)
    }
}
