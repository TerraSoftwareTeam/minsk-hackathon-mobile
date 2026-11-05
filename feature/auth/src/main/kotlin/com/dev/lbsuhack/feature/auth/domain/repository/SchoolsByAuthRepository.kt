package com.dev.lbsuhack.feature.auth.domain.repository

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.Pupil
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole

interface SchoolsByAuthRepository {
    suspend fun loginAndGetUser(
        username: String,
        password: String,
        role: UserRole = UserRole.PUPIL,
    ): DomainResult<User>

    suspend fun loginAndGetPupil(
        username: String,
        password: String,
    ): DomainResult<Pupil>
}
