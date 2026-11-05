package com.dev.lbsuhack.core.domain.user

import com.dev.lbsuhack.core.domain.model.User
import kotlinx.coroutines.flow.StateFlow

class ObserveCurrentUserUseCase(
    private val repository: UserRepository,
) {

    operator fun invoke(): StateFlow<User?> = repository.currentUser
}
