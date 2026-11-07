package com.dev.lbsuhack.core.data.user

import com.dev.lbsuhack.core.data.KeyValueStorage
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionClearHook
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
    private val storage: KeyValueStorage,
    private val httpClient: HttpClient,
    private val sessionRepository: SessionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UserRepository, SessionClearHook {

    init {
        storage.remove(LEGACY_FATHERS_NAME_KEY)
    }

    private val mutex = Mutex()
    private var cacheGeneration = 0L
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        val cached = readFromPrefs()
        if (cached != null && cached.id == (sessionRepository.state.value as? SessionState.Authenticated)?.user?.id) {
            _currentUser.value = cached
        }
    }

    private data class UserRequest(val userId: String, val schoolName: String?, val generation: Long)

    override suspend fun getCurrentUser(forceRefresh: Boolean): DomainResult<User> = withContext(ioDispatcher) {
        val request = mutex.withLock {
            val session = (sessionRepository.state.value as? SessionState.Authenticated)?.user
                ?: return@withContext DomainResult.Failure(DomainError.Unauthorized)
            if (session.id.isBlank()) return@withContext DomainResult.Failure(DomainError.Unauthorized)
            val cached = (_currentUser.value ?: readFromPrefs())?.takeIf { it.id == session.id && it.role == session.role }
            if (!forceRefresh && cached != null) {
                _currentUser.value = cached
                return@withContext DomainResult.Success(cached)
            }
            UserRequest(session.id, cached?.schoolName ?: session.schoolName, cacheGeneration)
        }
        val result = safeCall { httpClient.get("api/v1/users/${request.userId}").body<UserResponseDto>() }
        mutex.withLock {
            val currentId = (sessionRepository.state.value as? SessionState.Authenticated)?.user?.id
            if (currentId != request.userId || cacheGeneration != request.generation) {
                return@withLock DomainResult.Failure(DomainError.Unauthorized)
            }
            when (result) {
                is NetworkResult.Success -> {
                    if (result.data.id != request.userId) return@withLock DomainResult.Failure(DomainError.Unauthorized)
                    val user = result.data.toDomain(request.schoolName)
                    _currentUser.value = user
                    writeToPrefs(user)
                    DomainResult.Success(user)
                }
                is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
            }
        }
    }

    override suspend fun beforeClear() = clearCache()

    override suspend fun updateCache(user: User) {
        withContext(ioDispatcher) {
            mutex.withLock {
                cacheGeneration += 1
                _currentUser.value = user
                writeToPrefs(user)
            }
        }
    }

    override suspend fun clearCache() {
        withContext(ioDispatcher) {
            mutex.withLock {
                cacheGeneration += 1
                _currentUser.value = null
                storage.clear()
            }
        }
    }

    private fun readFromPrefs(): User? {
        val id = storage.getString(KEY_ID) ?: return null
        val name = storage.getString(KEY_NAME) ?: return null
        val surname = storage.getString(KEY_SURNAME)
        val schoolsById = storage.getString(KEY_SCHOOLS_BY_ID)
        val schoolName = storage.getString(KEY_SCHOOL_NAME)
        val roleStr = storage.getString(KEY_ROLE)
        val role = roleStr?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: UserRole.PUPIL
        val pupilClass = storage.getString(KEY_CLASS)

        return when (role) {
            UserRole.PUPIL -> User.Pupil(
                id = id,
                name = name,
                surname = surname,
                schoolsById = schoolsById,
                schoolName = schoolName,
                pupilClass = pupilClass.orEmpty(),
            )
            UserRole.TEACHER -> User.Teacher(
                id = id,
                name = name,
                surname = surname,
                schoolsById = schoolsById,
                schoolName = schoolName,
            )
            UserRole.ORGANIZATOR -> User.Organizator(
                id = id,
                name = name,
                surname = surname,
            )
        }
    }

    private fun writeToPrefs(user: User) {
        storage.putString(KEY_ID, user.id)
        storage.putString(KEY_NAME, user.name)
        storage.putString(KEY_SURNAME, user.surname)
        storage.putString(KEY_SCHOOLS_BY_ID, user.schoolsById)
        storage.putString(KEY_SCHOOL_NAME, user.schoolName)
        storage.putString(KEY_ROLE, user.role.name)

        if (user is User.Pupil) {
            storage.putString(KEY_CLASS, user.pupilClass)
        } else {
            storage.remove(KEY_CLASS)
        }
    }

    private fun UserResponseDto.toDomain(existingSchoolName: String? = null): User = when (role) {
        UserResponseDto.Role.PUPIL -> User.Pupil(
            id = id,
            name = name,
            surname = surname,
            schoolsById = schoolsById,
            schoolName = existingSchoolName,
            pupilClass = propertyClass.orEmpty(),
        )
        UserResponseDto.Role.TEACHER -> User.Teacher(
            id = id,
            name = name,
            surname = surname,
            schoolsById = schoolsById,
            schoolName = existingSchoolName,
        )
        UserResponseDto.Role.ADMIN -> User.Organizator(
            id = id,
            name = name,
            surname = surname,
        )
    }

    private companion object {
        const val LEGACY_FATHERS_NAME_KEY = "fathers_name"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_SURNAME = "surname"
        const val KEY_SCHOOLS_BY_ID = "schools_by_id"
        const val KEY_SCHOOL_NAME = "school_name"
        const val KEY_ROLE = "role"
        const val KEY_CLASS = "pupil_class"
    }
}
