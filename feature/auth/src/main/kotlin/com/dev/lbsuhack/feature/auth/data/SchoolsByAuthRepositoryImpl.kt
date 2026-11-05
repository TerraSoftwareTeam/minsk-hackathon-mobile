package com.dev.lbsuhack.feature.auth.data.repository

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.Pupil
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.auth.data.model.RemoteProfileResult
import com.dev.lbsuhack.feature.auth.data.parser.SchoolsByProfileParser
import com.dev.lbsuhack.feature.auth.data.remote.SchoolsByRemoteDataSource
import com.dev.lbsuhack.feature.auth.domain.repository.SchoolsByAuthRepository
import java.io.IOException
import java.net.SocketTimeoutException

class SchoolsByAuthRepositoryImpl(
    private val remoteDataSource: SchoolsByRemoteDataSource,
    private val parser: SchoolsByProfileParser,
) : SchoolsByAuthRepository {

    override suspend fun loginAndGetUser(
        username: String,
        password: String,
        role: UserRole,
    ): DomainResult<User> {
        if (username.isBlank() || password.isBlank()) {
            return DomainResult.Failure(DomainError.Validation)
        }

        return when (val result = remoteDataSource.loginAndFetchProfileHtml(username.trim(), password)) {
            is RemoteProfileResult.Success -> {
                val user = parser.parse(html = result.html, role = role, pageUrl = result.profileUrl)
                if (user != null) {
                    DomainResult.Success(user)
                } else {
                    DomainResult.Failure(
                        DomainError.BusinessRule(
                            code = "PARSE_ERROR",
                            message = "Could not parse $role profile from Schools.by page"
                        )
                    )
                }
            }

            is RemoteProfileResult.InvalidCredentials -> {
                DomainResult.Failure(DomainError.Unauthorized)
            }

            is RemoteProfileResult.Error -> {
                val domainError = when (result.cause) {
                    is SocketTimeoutException -> DomainError.Timeout
                    is IOException -> DomainError.Network
                    else -> DomainError.Exception(result.cause)
                }
                DomainResult.Failure(domainError)
            }
        }
    }

    override suspend fun loginAndGetPupil(
        username: String,
        password: String,
    ): DomainResult<Pupil> {
        return when (val result = loginAndGetUser(username, password, UserRole.PUPIL)) {
            is DomainResult.Success -> {
                val user = result.value
                if (user is Pupil) {
                    DomainResult.Success(user)
                } else {
                    DomainResult.Failure(
                        DomainError.BusinessRule(
                            code = "ROLE_MISMATCH",
                            message = "Expected pupil profile but got ${user::class.simpleName}"
                        )
                    )
                }
            }
            is DomainResult.Failure -> result
        }
    }
}
