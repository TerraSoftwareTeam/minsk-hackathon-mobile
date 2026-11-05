package com.dev.lbsuhack.feature.auth.data

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.auth.domain.AuthRepository
import com.dev.lbsuhack.feature.auth.domain.repository.SchoolsByAuthRepository

class AuthRepositoryImpl(
    private val schoolsByRepository: SchoolsByAuthRepository,
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String,
        role: UserRole,
    ): DomainResult<SessionUser> {
        return when (val result = schoolsByRepository.loginAndGetUser(username, password, role)) {
            is DomainResult.Success -> {
                val user = result.value
                val sessionUser = SessionUser(
                    id = user.id.toLongOrNull() ?: 1L,
                    name = user.name,
                    email = "$username@schools.by",
                    role = user.role,
                    pupilClass = (user as? User.Pupil)?.pupilClass,
                )
                DomainResult.Success(sessionUser)
            }
            is DomainResult.Failure -> DomainResult.Failure(result.error)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): DomainResult<SessionUser> = DomainResult.Success(
        SessionUser(
            id = 2L,
            name = name,
            email = email,
            role = UserRole.PUPIL,
        ),
    )
}
