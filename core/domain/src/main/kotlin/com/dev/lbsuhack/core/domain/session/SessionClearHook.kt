package com.dev.lbsuhack.core.domain.session

fun interface SessionClearHook {

    suspend fun beforeClear()
}
