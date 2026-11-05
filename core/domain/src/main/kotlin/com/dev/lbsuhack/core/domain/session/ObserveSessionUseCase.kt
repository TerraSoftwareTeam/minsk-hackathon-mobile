package com.dev.lbsuhack.core.domain.session

import kotlinx.coroutines.flow.StateFlow

class ObserveSessionUseCase(
    private val repository: SessionRepository,
) {

    val state: StateFlow<SessionState>
        get() = repository.state
}
