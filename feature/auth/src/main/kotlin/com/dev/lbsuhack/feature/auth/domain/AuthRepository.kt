package com.dev.lbsuhack.feature.auth.domain

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.SessionUser

interface AuthRepository {

    suspend fun login(
        email: String,
        password: String,
    ): DomainResult<SessionUser>

    suspend fun register(
        name: String,
        email: String,
        password: String,
    ): DomainResult<SessionUser>
}
