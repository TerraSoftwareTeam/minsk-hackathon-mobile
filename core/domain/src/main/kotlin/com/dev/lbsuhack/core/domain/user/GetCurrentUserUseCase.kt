package com.dev.lbsuhack.core.domain.user

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User

class GetCurrentUserUseCase(
    private val repository: UserRepository,
) {

    suspend operator fun invoke(forceRefresh: Boolean = false): DomainResult<User> =
        repository.getCurrentUser(forceRefresh = forceRefresh)
}
