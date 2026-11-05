package com.dev.lbsuhack.core.domain.session

import com.dev.lbsuhack.core.domain.model.UserRole
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
    val id: String,
    val name: String,
    val surname: String? = null,
    val fathersName: String? = null,
    val schoolsById: String? = null,
    val schoolName: String? = null,
    val role: UserRole = UserRole.PUPIL,
    val pupilClass: String? = null,
)
