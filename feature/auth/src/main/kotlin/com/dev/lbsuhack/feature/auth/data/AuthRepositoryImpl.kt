package com.dev.lbsuhack.feature.auth.data

import android.util.Log
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.domain.user.UserRepository
import com.dev.lbsuhack.core.network.auth.TokenStorage
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.error.NetworkError
import com.dev.lbsuhack.core.network.error.toDomain
import com.dev.lbsuhack.core.network.generated.model.AuthResponseDto
import com.dev.lbsuhack.core.network.generated.model.LoginDto
import com.dev.lbsuhack.core.network.generated.model.RegisterDto
import com.dev.lbsuhack.feature.auth.domain.AuthRepository
import com.dev.lbsuhack.feature.auth.parseSchoolClass
import com.dev.lbsuhack.feature.auth.domain.repository.SchoolsByAuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val schoolsByRepository: SchoolsByAuthRepository,
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String,
        role: UserRole,
    ): DomainResult<SessionUser> = withContext(ioDispatcher) {
        if (username.isBlank()) return@withContext DomainResult.Failure(DomainError.Validation)
        if (role == UserRole.ORGANIZATOR) {
            return@withContext authenticate(LoginDto(role = LoginDto.Role.ADMIN, id = username.trim()))
                .toDomainResult()
                .toSessionResult(expectedRole = UserRole.ORGANIZATOR)
        }
        when (val result = schoolsByRepository.loginAndGetUser(username.trim(), password, role)) {
            is DomainResult.Failure -> result
            is DomainResult.Success -> {
                val user = result.value
                registerPupil(user)
                    .toDomainResult()
                    .toSessionResult(expectedRole = role, schoolName = user.schoolName)
            }
        }
    }

    override suspend fun registerTeacher(
        schoolsById: String,
        name: String,
        surname: String,
        className: String,
    ): DomainResult<SessionUser> = withContext(ioDispatcher) {
        if (schoolsById.isBlank() || name.isBlank() || surname.isBlank() || className.isBlank()) {
            return@withContext DomainResult.Failure(DomainError.Validation)
        }
        safeCall {
            httpClient.post("api/v1/auth/register") {
                expectSuccess = true
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterDto(
                        schoolsById = schoolsById,
                        name = name,
                        surname = surname,
                        role = RegisterDto.Role.TEACHER,
                        propertyClass = className,
                    ),
                )
            }.body<AuthResponseDto>()
        }
            .toDomainResult()
            .toSessionResult(expectedRole = UserRole.TEACHER)
    }

    private suspend fun authenticate(dto: LoginDto): NetworkResult<AuthResponseDto> = safeCall {
        httpClient.post("api/v1/auth/login") {
            expectSuccess = true
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body<AuthResponseDto>()
    }

    private suspend fun registerPupil(user: User): NetworkResult<AuthResponseDto> {
        val schoolClass = (user as? User.Pupil)?.pupilClass?.let(::parseSchoolClass)
        val missingFields = buildList {
            if (user.name.isBlank()) add("name")
            if (user.surname.isNullOrBlank()) add("surname")
            if (schoolClass.isNullOrBlank()) add("class")
        }
        if (missingFields.isNotEmpty()) {
            Log.w("AuthRepository", "Registration not sent: Schools.by profile is missing ${missingFields.joinToString()}")
            return NetworkResult.Error(NetworkError.ClientError(400,
                "В профиле Schools.by отсутствуют данные для регистрации: ${missingFields.joinToString()}."))
        }
        val result = safeCall {
            httpClient.post("api/v1/auth/register") {
                expectSuccess = true
                contentType(ContentType.Application.Json)
                setBody(RegisterDto(
                    schoolsById = user.schoolsById ?: user.id,
                    name = user.name,
                    surname = requireNotNull(user.surname),
                    role = RegisterDto.Role.PUPIL,
                    propertyClass = requireNotNull(schoolClass),
                ))
            }.body<AuthResponseDto>()
        }
        when (result) {
            is NetworkResult.Success -> Log.i("AuthRepository", "Backend registration succeeded")
            is NetworkResult.Error -> Log.w("AuthRepository", "Backend registration failed: ${result.error::class.simpleName}")
        }
        return result
    }

    private fun NetworkResult<AuthResponseDto>.toDomainResult(): DomainResult<AuthResponseDto> = when (this) {
        is NetworkResult.Success -> DomainResult.Success(data)
        is NetworkResult.Error -> DomainResult.Failure(error.toDomain())
    }

    private suspend fun DomainResult<AuthResponseDto>.toSessionResult(
        expectedRole: UserRole,
        schoolName: String? = null,
    ): DomainResult<SessionUser> = when (this) {
        is DomainResult.Failure -> this
        is DomainResult.Success -> {
            val actualRole = value.user.toSessionUser().role
            if (actualRole != expectedRole) {
                Log.w("AuthRepository", "Backend returned role $actualRole, expected $expectedRole")
                DomainResult.Failure(DomainError.BusinessRule(ROLE_MISMATCH, actualRole.name))
            } else {
                when (val saved = safeCall {
                    require(value.accessToken.isNotBlank())
                    userRepository.updateCache(value.user.toDomain(schoolName))
                    tokenStorage.saveTokens(value.accessToken, null)
                    value.user.toSessionUser(schoolName)
                }) {
                    is NetworkResult.Success -> DomainResult.Success(saved.data)
                    is NetworkResult.Error -> DomainResult.Failure(saved.error.toDomain())
                }
            }
        }
    }

    internal companion object {
        const val ROLE_MISMATCH = "ROLE_MISMATCH"
    }
}
