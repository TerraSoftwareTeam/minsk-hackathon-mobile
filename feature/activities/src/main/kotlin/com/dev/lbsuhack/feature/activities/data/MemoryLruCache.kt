package com.dev.lbsuhack.feature.activities.data

internal class MemoryLruCache<K : Any, V : Any>(
    private val maxSize: Int,
    private val sizeOf: (V) -> Int,
) {

    private val entries = LinkedHashMap<K, V>(16, 0.75f, true)
    private var totalSize = 0

    @Synchronized
    fun get(key: K): V? = entries[key]

    @Synchronized
    fun put(key: K, value: V) {
        entries.remove(key)?.let { totalSize -= sizeOf(it) }
        entries[key] = value
        totalSize += sizeOf(value)
        while (totalSize > maxSize) {
            val eldest = entries.entries.firstOrNull() ?: break
            entries.remove(eldest.key)
            totalSize -= sizeOf(eldest.value)
        }
    }
}
