package com.dev.lbsuhack.feature.auth.domain.usecase

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.Pupil
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.auth.domain.repository.SchoolsByAuthRepository

class LoginSchoolsByUseCase(
    private val repository: SchoolsByAuthRepository,
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        role: UserRole = UserRole.PUPIL,
    ): DomainResult<User> =
        repository.loginAndGetUser(username = username, password = password, role = role)

    suspend fun asPupil(
        username: String,
        password: String,
    ): DomainResult<Pupil> =
        repository.loginAndGetPupil(username = username, password = password)
}
