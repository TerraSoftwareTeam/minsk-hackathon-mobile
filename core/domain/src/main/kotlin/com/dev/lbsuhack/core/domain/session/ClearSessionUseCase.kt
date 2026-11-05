package com.dev.lbsuhack.core.domain.session

class ClearSessionUseCase(
    private val repository: SessionRepository,
) {

    suspend operator fun invoke() {
        repository.clear()
    }
}
