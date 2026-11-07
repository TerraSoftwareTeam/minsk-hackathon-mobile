package com.dev.lbsuhack.core.data

class FakeKeyValueStorage(
    initial: Map<String, String> = emptyMap(),
) : KeyValueStorage {

    val values = initial.toMutableMap()

    override fun getString(key: String): String? = values[key]

    override fun putString(key: String, value: String?) {
        if (value == null) {
            values.remove(key)
        } else {
            values[key] = value
        }
    }

    override fun remove(key: String) {
        values.remove(key)
    }

    override fun clear() {
        values.clear()
    }
}
