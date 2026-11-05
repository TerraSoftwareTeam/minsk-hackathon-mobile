package com.dev.lbsuhack.core.domain.session

class RestoreSessionUseCase(
    private val repository: SessionRepository,
) {

    suspend operator fun invoke() {
        repository.restore()
    }
}
