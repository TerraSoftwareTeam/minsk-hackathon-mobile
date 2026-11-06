package com.dev.lbsuhack.core.domain.push

interface PushTokenRepository {

    suspend fun register(token: String): Boolean

    suspend fun unregister(token: String): Boolean
}

class RegisterPushTokenUseCase(
    private val repository: PushTokenRepository,
) {

    suspend operator fun invoke(token: String): Boolean = repository.register(token)
}

class UnregisterPushTokenUseCase(
    private val repository: PushTokenRepository,
) {

    suspend operator fun invoke(token: String): Boolean = repository.unregister(token)
}
