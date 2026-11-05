package com.dev.lbsuhack.core.domain.user

import com.dev.lbsuhack.core.domain.model.User

class UpdateUserCacheUseCase(
    private val repository: UserRepository,
) {

    suspend operator fun invoke(user: User) {
        repository.updateCache(user)
    }
}
