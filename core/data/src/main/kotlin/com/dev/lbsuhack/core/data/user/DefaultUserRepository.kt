package com.dev.lbsuhack.core.data.user

import android.content.Context
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionRepository
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.user.UserRepository
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.error.toDomain
import com.dev.lbsuhack.core.network.generated.model.UserResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DefaultUserRepository(
    context: Context,
    private val httpClient: HttpClient,
    private val sessionRepository: SessionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UserRepository {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val mutex = Mutex()
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        val cached = readFromPrefs()
        if (cached != null) {
            _currentUser.value = cached
        }
    }

    override suspend fun getCurrentUser(forceRefresh: Boolean): DomainResult<User> = withContext(ioDispatcher) {
        mutex.withLock {
            val inMemory = _currentUser.value
            if (!forceRefresh && inMemory != null) {
                return@withLock DomainResult.Success(inMemory)
            }

            val cached = readFromPrefs()
            if (!forceRefresh && cached != null) {
                _currentUser.value = cached
                return@withLock DomainResult.Success(cached)
            }

            val sessionState = sessionRepository.state.value
            val userId = when (sessionState) {
                is SessionState.Authenticated -> sessionState.user.id
                else -> null
            }

            if (userId.isNullOrBlank()) {
                return@withLock DomainResult.Failure(DomainError.Unauthorized)
            }

            val result: NetworkResult<UserResponseDto> = safeCall {
                httpClient.get("api/v1/users/$userId").body()
            }

            when (result) {
                is NetworkResult.Success -> {
                    val domainUser = result.data.toDomain()
                    _currentUser.value = domainUser
                    writeToPrefs(domainUser)
                    DomainResult.Success(domainUser)
                }
                is NetworkResult.Error -> {
                    if (cached != null) {
                        DomainResult.Success(cached)
                    } else {
                        DomainResult.Failure(result.error.toDomain())
                    }
                }
            }
        }
    }

    override suspend fun updateCache(user: User) {
        withContext(ioDispatcher) {
            mutex.withLock {
                _currentUser.value = user
                writeToPrefs(user)
            }
        }
    }

    override suspend fun clearCache() {
        withContext(ioDispatcher) {
            mutex.withLock {
                _currentUser.value = null
                preferences.edit().clear().apply()
            }
        }
    }

    private fun readFromPrefs(): User? {
        val id = preferences.getString(KEY_ID, null) ?: return null
        val name = preferences.getString(KEY_NAME, null) ?: return null
        val surname = preferences.getString(KEY_SURNAME, null)
        val fathersName = preferences.getString(KEY_FATHERS_NAME, null)
        val schoolsById = preferences.getString(KEY_SCHOOLS_BY_ID, null)
        val schoolName = preferences.getString(KEY_SCHOOL_NAME, null)
        val roleStr = preferences.getString(KEY_ROLE, null)
        val role = roleStr?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: UserRole.PUPIL
        val pupilClass = preferences.getString(KEY_CLASS, null)

        return when (role) {
            UserRole.PUPIL -> User.Pupil(
                id = id,
                name = name,
                surname = surname,
                fathersName = fathersName,
                schoolsById = schoolsById,
                schoolName = schoolName,
                pupilClass = pupilClass.orEmpty(),
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
    }

    private fun writeToPrefs(user: User) {
        val editor = preferences.edit()
            .putString(KEY_ID, user.id)
            .putString(KEY_NAME, user.name)
            .putString(KEY_SURNAME, user.surname)
            .putString(KEY_FATHERS_NAME, user.fathersName)
            .putString(KEY_SCHOOLS_BY_ID, user.schoolsById)
            .putString(KEY_SCHOOL_NAME, user.schoolName)
            .putString(KEY_ROLE, user.role.name)

        if (user is User.Pupil) {
            editor.putString(KEY_CLASS, user.pupilClass)
        } else {
            editor.remove(KEY_CLASS)
        }
        editor.apply()
    }

    private fun UserResponseDto.toDomain(): User = when (role) {
        UserResponseDto.Role.PUPIL -> User.Pupil(
            id = id,
            name = name,
            surname = surname,
            fathersName = fathersName,
            schoolsById = schoolsById,
            schoolName = null,
            pupilClass = propertyClass.orEmpty(),
        )
        UserResponseDto.Role.TEACHER -> User.Teacher(
            id = id,
            name = name,
            surname = surname,
            fathersName = fathersName,
            schoolsById = schoolsById,
            schoolName = null,
        )
        UserResponseDto.Role.ADMIN -> User.Organizator(
            id = id,
            name = name,
            surname = surname,
            fathersName = fathersName,
        )
    }

    private companion object {
        const val PREFS_NAME = "cached_user_profile"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_SURNAME = "surname"
        const val KEY_FATHERS_NAME = "fathers_name"
        const val KEY_SCHOOLS_BY_ID = "schools_by_id"
        const val KEY_SCHOOL_NAME = "school_name"
        const val KEY_ROLE = "role"
        const val KEY_CLASS = "pupil_class"
    }
}
