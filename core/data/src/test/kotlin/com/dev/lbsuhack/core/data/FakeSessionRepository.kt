package com.dev.lbsuhack.core.data

import com.dev.lbsuhack.core.domain.session.SessionRepository
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionRepository(
    initial: SessionState = SessionState.Unknown,
) : SessionRepository {

    private val _state = MutableStateFlow(initial)

    override val state: StateFlow<SessionState> = _state.asStateFlow()

    override suspend fun restore() = Unit

    override suspend fun save(user: SessionUser) {
        _state.value = SessionState.Authenticated(user)
    }

    override suspend fun clear() {
        _state.value = SessionState.Unauthenticated
    }

    fun setState(value: SessionState) {
        _state.value = value
    }
}
