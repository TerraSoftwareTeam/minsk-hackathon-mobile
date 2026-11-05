package com.dev.lbsuhack.core.domain.session

class SaveSessionUseCase(
    private val repository: SessionRepository,
) {

    suspend operator fun invoke(user: SessionUser) {
        repository.save(user)
    }
}
