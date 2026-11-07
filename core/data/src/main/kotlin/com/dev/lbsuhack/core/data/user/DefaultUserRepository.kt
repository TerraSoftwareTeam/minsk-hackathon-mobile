package com.dev.lbsuhack.core.data.user

import com.dev.lbsuhack.core.data.KeyValueStorage
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
    private val storage: KeyValueStorage,
    private val httpClient: HttpClient,
    private val sessionRepository: SessionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UserRepository {

    init {
        storage.remove(LEGACY_FATHERS_NAME_KEY)
    }

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
            val sessionState = sessionRepository.state.value
            val userId = (sessionState as? SessionState.Authenticated)?.user?.id
            if (userId.isNullOrBlank()) {
                return@withLock DomainResult.Failure(DomainError.Unauthorized)
            }
            val inMemory = _currentUser.value?.takeIf { it.id == userId }
            val cached = readFromPrefs()?.takeIf { it.id == userId }
            if (!forceRefresh && (inMemory != null || cached != null)) {
                val user = requireNotNull(inMemory ?: cached)
                _currentUser.value = user
                return@withLock DomainResult.Success(user)
            }

            val result: NetworkResult<UserResponseDto> = safeCall {
                httpClient.get("api/v1/users/$userId").body()
            }

            when (result) {
                is NetworkResult.Success -> {
                    val existingSchoolName = inMemory?.schoolName
                        ?: cached?.schoolName
                        ?: (sessionState as? SessionState.Authenticated)?.user?.schoolName
                    val domainUser = result.data.toDomain(existingSchoolName)
                    _currentUser.value = domainUser
                    writeToPrefs(domainUser)
                    DomainResult.Success(domainUser)
                }
                is NetworkResult.Error -> {
                    DomainResult.Failure(result.error.toDomain())
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
