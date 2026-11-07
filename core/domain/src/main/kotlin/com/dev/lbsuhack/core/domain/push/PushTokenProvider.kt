package com.dev.lbsuhack.core.domain.push

interface PushTokenProvider {

    suspend fun currentToken(): String?
}
