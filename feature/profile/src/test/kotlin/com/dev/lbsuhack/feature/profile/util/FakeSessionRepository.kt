package com.dev.lbsuhack.feature.profile.util

import com.dev.lbsuhack.core.domain.session.SessionRepository
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionRepository(
    initialState: SessionState = SessionState.Unauthenticated,
) : SessionRepository {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    var cleared: Boolean = false
        private set

    override suspend fun restore() = Unit

    override suspend fun save(user: SessionUser) {
        _state.value = SessionState.Authenticated(user)
    }

    override suspend fun clear() {
        cleared = true
        _state.value = SessionState.Unauthenticated
    }
}
