package com.dev.lbsuhack.feature.requests.store

import com.dev.lbsuhack.feature.requests.store.requests.defaultStartsAt
import com.dev.lbsuhack.feature.requests.store.requests.isFutureDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime

class StartsAtFormatTest {

    @Test
    fun defaultStartsAtIsIsoWithSecondsAndOffset() {
        val now = OffsetDateTime.parse("2026-11-06T17:45:12+03:00")
        val value = defaultStartsAt(now)
        assertEquals("2026-11-13T12:00:00+03:00", value)
        assertTrue(isFutureDate(value, now))
    }
}
