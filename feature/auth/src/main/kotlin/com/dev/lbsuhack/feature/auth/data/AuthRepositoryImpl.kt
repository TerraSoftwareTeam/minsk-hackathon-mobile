package com.dev.lbsuhack.feature.auth.data

import android.util.Log
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.domain.user.UserRepository
import com.dev.lbsuhack.core.network.auth.TokenStorage
import com.dev.lbsuhack.core.network.generated.model.AuthResponseDto
import com.dev.lbsuhack.core.network.generated.model.LoginDto
import com.dev.lbsuhack.core.network.generated.model.RegisterDto
import com.dev.lbsuhack.core.network.generated.model.UserResponseDto
import com.dev.lbsuhack.feature.auth.domain.AuthRepository
import com.dev.lbsuhack.feature.auth.domain.repository.SchoolsByAuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
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
        Log.d(TAG, "Login started for username=$username, role=$role")

        if (role == UserRole.ORGANIZATOR) {
            return@withContext loginOrganizer(username.trim())
        }

        val schoolsResult = schoolsByRepository.loginAndGetUser(username, password, role)
        if (schoolsResult is DomainResult.Failure) {
            Log.e(TAG, "Schools.by authentication failed: ${schoolsResult.error}")
            return@withContext DomainResult.Failure(schoolsResult.error)
        }

        val parsedUser = (schoolsResult as DomainResult.Success).value
        Log.d(TAG, "Schools.by authentication succeeded for user=${parsedUser.name}, id=${parsedUser.id}")

        val backendResult = syncUserWithBackend(parsedUser, role)
        if (backendResult is DomainResult.Success) {
            backendResult
        } else {
            fallbackToLocalSession(parsedUser)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): DomainResult<SessionUser> {
        return DomainResult.Failure(
            DomainError.BusinessRule(
                code = "UNSUPPORTED",
                message = "Registration is handled via Schools.by or administrator invite",
            )
        )
    }

    private suspend fun loginOrganizer(organizerId: String): DomainResult<SessionUser> {
        return try {
            val response: HttpResponse = httpClient.post("api/v1/auth/login") {
                setBody(
                    LoginDto(
                        role = LoginDto.Role.ORGANIZATOR,
                        id = organizerId,
                    )
                )
            }
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val authResponse = response.body<AuthResponseDto>()
                tokenStorage.saveTokens(accessToken = authResponse.accessToken, refreshToken = null)
                val domainUser = authResponse.user.toDomain()
                userRepository.updateCache(domainUser)
                DomainResult.Success(authResponse.user.toSessionUser())
            } else if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound) {
                Log.e(TAG, "Organizer login unauthorized: ${response.status.value}")
                DomainResult.Failure(DomainError.Unauthorized)
            } else {
                val errorBody = response.bodyAsText()
                Log.e(TAG, "Organizer login failed: ${response.status.value}, body=$errorBody")
                DomainResult.Failure(DomainError.Network)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Organizer login exception", e)
            DomainResult.Failure(DomainError.Exception(e))
        }
    }

    private suspend fun syncUserWithBackend(
        parsedUser: User,
        role: UserRole,
    ): DomainResult<SessionUser>? {
        val dtoRole = if (role == UserRole.TEACHER) LoginDto.Role.TEACHER else LoginDto.Role.PUPIL

        return try {
            val loginResponse: HttpResponse = httpClient.post("api/v1/auth/login") {
                setBody(
                    LoginDto(
                        role = dtoRole,
                        schoolsById = parsedUser.id,
                    )
                )
            }

            if (loginResponse.status == HttpStatusCode.OK || loginResponse.status == HttpStatusCode.Created) {
                val authResponse = loginResponse.body<AuthResponseDto>()
                tokenStorage.saveTokens(accessToken = authResponse.accessToken, refreshToken = null)
                val domainUser = authResponse.user.toDomain()
                userRepository.updateCache(domainUser)
                Log.d(TAG, "Backend login succeeded for ${domainUser.name}")
                return DomainResult.Success(authResponse.user.toSessionUser())
            }

            if (loginResponse.status == HttpStatusCode.Unauthorized || loginResponse.status == HttpStatusCode.NotFound) {
                Log.d(TAG, "User not found on backend (${loginResponse.status.value}), attempting registration")
                return registerUserOnBackend(parsedUser, role)
            }

            val errorBody = loginResponse.bodyAsText()
            Log.w(TAG, "Backend login returned ${loginResponse.status.value}: $errorBody")
            null
        } catch (e: Exception) {
            Log.w(TAG, "Backend login request failed", e)
            null
        }
    }

    private suspend fun registerUserOnBackend(
        parsedUser: User,
        role: UserRole,
    ): DomainResult<SessionUser>? {
        val registerRole = if (role == UserRole.TEACHER) RegisterDto.Role.TEACHER else RegisterDto.Role.PUPIL
        val pupilClass = (parsedUser as? User.Pupil)?.pupilClass?.ifBlank { null } ?: "10A"
        val schoolName = parsedUser.schoolName?.ifBlank { null } ?: "Школа №1"
        val fathersName = parsedUser.fathersName?.ifBlank { null } ?: "Иванович"
        val surname = parsedUser.surname?.ifBlank { null } ?: parsedUser.name

        return try {
            val registerResponse: HttpResponse = httpClient.post("api/v1/auth/register") {
                setBody(
                    RegisterDto(
                        schoolsById = parsedUser.id,
                        name = parsedUser.name,
                        surname = surname,
                        fathersName = fathersName,
                        role = registerRole,
                        propertyClass = pupilClass,
                        schoolName = schoolName,
                    )
                )
            }

            if (registerResponse.status == HttpStatusCode.OK || registerResponse.status == HttpStatusCode.Created) {
                val authResponse = registerResponse.body<AuthResponseDto>()
                tokenStorage.saveTokens(accessToken = authResponse.accessToken, refreshToken = null)
                val domainUser = authResponse.user.toDomain()
                userRepository.updateCache(domainUser)
                Log.d(TAG, "Backend registration succeeded for ${domainUser.name}")
                DomainResult.Success(authResponse.user.toSessionUser())
            } else {
                val errorBody = registerResponse.bodyAsText()
                Log.w(TAG, "Backend registration returned ${registerResponse.status.value}: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Backend register request failed", e)
            null
        }
    }

    private suspend fun fallbackToLocalSession(parsedUser: User): DomainResult<SessionUser> {
        Log.i(TAG, "Using local session for user=${parsedUser.name}, id=${parsedUser.id}")
        userRepository.updateCache(parsedUser)
        return DomainResult.Success(parsedUser.toSessionUser())
    }

    private fun User.toSessionUser(): SessionUser = SessionUser(
        id = id,
        name = name,
        surname = surname,
        fathersName = fathersName,
        schoolsById = schoolsById ?: id,
        schoolName = schoolName,
        role = role,
        pupilClass = (this as? User.Pupil)?.pupilClass,
    )

    private fun UserResponseDto.toSessionUser(): SessionUser = SessionUser(
        id = id,
        name = name,
        surname = surname,
        fathersName = fathersName,
        schoolsById = schoolsById,
        schoolName = schoolName,
        role = role.toDomainRole(),
        pupilClass = propertyClass,
    )

    private fun UserResponseDto.toDomain(): User = when (role.toDomainRole()) {
        UserRole.PUPIL -> User.Pupil(
            id = id,
            name = name,
            surname = surname,
            fathersName = fathersName,
            schoolsById = schoolsById,
            schoolName = schoolName,
            pupilClass = propertyClass.orEmpty(),
        )
        UserRole.TEACHER -> User.Teacher(
            id = id,
            name = name,
            surname = surname,
            fathersName = fathersName,
            schoolsById = schoolsById,
            schoolName = schoolName,
        )
        UserRole.ORGANIZATOR -> User.Organizator(
            id = id,
            name = name,
            surname = surname,
            fathersName = fathersName,
        )
    }

    private fun UserResponseDto.Role.toDomainRole(): UserRole = when (this) {
        UserResponseDto.Role.PUPIL -> UserRole.PUPIL
        UserResponseDto.Role.TEACHER -> UserRole.TEACHER
        UserResponseDto.Role.ORGANIZATOR -> UserRole.ORGANIZATOR
    }

    private companion object {
        private const val TAG = "AuthRepository"
    }
}
