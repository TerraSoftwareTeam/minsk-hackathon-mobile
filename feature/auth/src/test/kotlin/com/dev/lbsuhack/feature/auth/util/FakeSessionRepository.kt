package com.dev.lbsuhack.feature.auth.util

import com.dev.lbsuhack.core.domain.session.SessionRepository
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionRepository : SessionRepository {

    private val _state = MutableStateFlow<SessionState>(SessionState.Unauthenticated)
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    var savedUser: SessionUser? = null
        private set

    override suspend fun restore() = Unit

    override suspend fun save(user: SessionUser) {
        savedUser = user
        _state.value = SessionState.Authenticated(user)
    }

    override suspend fun clear() {
        savedUser = null
        _state.value = SessionState.Unauthenticated
    }
}
