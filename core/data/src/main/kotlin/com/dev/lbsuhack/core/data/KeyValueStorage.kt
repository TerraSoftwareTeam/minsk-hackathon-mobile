package com.dev.lbsuhack.core.data

import android.content.Context

interface KeyValueStorage {

    fun getString(key: String): String?

    fun putString(key: String, value: String?)

    fun remove(key: String)

    fun clear()
}

class PrefsKeyValueStorage(
    context: Context,
    name: String,
) : KeyValueStorage {

    private val preferences = context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String?) {
        preferences.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }
}
