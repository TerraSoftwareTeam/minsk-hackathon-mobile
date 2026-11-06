package com.dev.lbsuhack.feature.activities.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActivityLinkTest {
    @Test
    fun roundTripPreservesEventIdentifiers() {
        listOf("act-1", "a7b57ded-4bc1-41e3-a095-f044d9060b04", "встреча 1", "a?b#c").forEach { id ->
            assertEquals(id, ActivityLink.parse(ActivityLink.create(id)))
        }
    }

    @Test
    fun rejectsForeignOrMalformedLinks() {
        listOf(
            null,
            "",
            "https://example.com/act-1",
            "kola://profile/act-1",
            "kola://activity/",
            "kola://activity/act-1/extra",
            "kola://activity/act-1%2Fextra",
            "kola://activity/act-1?join=true",
            "kola://activity/act-1#fragment",
            "kola://user@activity/act-1",
            "kola://activity:42/act-1",
            "kola://activity/%ZZ",
            "kola://activity/" + "a".repeat(129),
        ).forEach { assertNull(it, ActivityLink.parse(it)) }
    }
}
