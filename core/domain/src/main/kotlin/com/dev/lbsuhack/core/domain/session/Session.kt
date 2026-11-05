package com.dev.lbsuhack.core.domain.session

import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {

    val state: StateFlow<SessionState>

    suspend fun restore()

    suspend fun save(user: SessionUser)

    suspend fun clear()
}

sealed interface SessionState {

    data object Unknown : SessionState

    data object Unauthenticated : SessionState

    data class Authenticated(val user: SessionUser) : SessionState
}

data class SessionUser(
    val id: Long,
    val name: String,
    val email: String,
)
