package com.dev.lbsuhack.feature.requests.store

import com.dev.lbsuhack.feature.requests.store.requests.defaultStartsAt
import com.dev.lbsuhack.feature.requests.store.requests.isFutureDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime

class StartsAtFormatTest {

    @Test
    fun defaultStartsAtIsSevenDaysAheadAtNoon() {
        val now = OffsetDateTime.parse("2026-11-06T17:45:12+03:00")
        val value = defaultStartsAt(now)
        assertEquals("2026-11-13T12:00+03:00", value.toString())
        assertTrue(isFutureDate(value, now))
    }

    @Test
    fun isFutureDateRejectsPastAndNullValues() {
        val now = OffsetDateTime.parse("2026-11-06T17:45:12+03:00")

        assertFalse(isFutureDate(now.minusDays(1), now))
        assertFalse(isFutureDate(null, now))
        assertTrue(isFutureDate(now.plusMinutes(1), now))
    }
}
