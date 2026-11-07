package com.dev.lbsuhack.feature.activities.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemoryLruCacheTest {

    @Test
    fun returnsStoredValue() {
        val cache = MemoryLruCache<String, Int>(maxSize = 10) { it }
        cache.put("a", 1)
        assertEquals(1, cache.get("a"))
    }

    @Test
    fun returnsNullForUnknownKey() {
        val cache = MemoryLruCache<String, Int>(maxSize = 10) { it }
        assertNull(cache.get("missing"))
    }

    @Test
    fun evictsLeastRecentlyUsedWhenSizeLimitIsExceeded() {
        val cache = MemoryLruCache<String, Int>(maxSize = 3) { it }
        cache.put("a", 1)
        cache.put("b", 1)
        cache.put("c", 1)
        cache.put("d", 1)
        assertNull(cache.get("a"))
        assertEquals(1, cache.get("d"))
    }

    @Test
    fun keepsEntryThatWasRecentlyRead() {
        val cache = MemoryLruCache<String, Int>(maxSize = 3) { it }
        cache.put("a", 1)
        cache.put("b", 1)
        cache.put("c", 1)
        cache.get("a")
        cache.put("d", 1)
        assertEquals(1, cache.get("a"))
        assertNull(cache.get("b"))
    }

    @Test
    fun replacingValueUpdatesAccountedSize() {
        val cache = MemoryLruCache<String, Int>(maxSize = 8) { it }
        cache.put("a", 4)
        cache.put("a", 4)
        cache.put("b", 6)
        assertNull(cache.get("a"))
        assertEquals(6, cache.get("b"))
    }
}
