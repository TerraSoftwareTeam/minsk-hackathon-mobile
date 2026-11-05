package com.dev.lbsuhack.feature.auth.data

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.auth.domain.AuthRepository

class AuthRepositoryImpl : AuthRepository {

    override suspend fun login(
        email: String,
        password: String,
    ): DomainResult<SessionUser> = DomainResult.Success(
        SessionUser(
            id = 1L,
            name = email.substringBefore('@').replaceFirstChar(Char::uppercase),
            email = email,
        ),
    )

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): DomainResult<SessionUser> = DomainResult.Success(
        SessionUser(
            id = 2L,
            name = name,
            email = email,
        ),
    )
}
