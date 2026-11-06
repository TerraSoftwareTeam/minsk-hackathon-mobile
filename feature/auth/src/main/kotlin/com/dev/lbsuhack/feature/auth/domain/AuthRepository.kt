package com.dev.lbsuhack.feature.auth.domain

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionUser

interface AuthRepository {

    suspend fun login(
        username: String,
        password: String,
        role: UserRole = UserRole.PUPIL,
    ): DomainResult<SessionUser>

    suspend fun registerOrganizer(schoolsById: String): DomainResult<SessionUser>

    suspend fun registerTeacher(
        schoolsById: String,
        name: String,
        surname: String,
        className: String,
    ): DomainResult<SessionUser>
}
