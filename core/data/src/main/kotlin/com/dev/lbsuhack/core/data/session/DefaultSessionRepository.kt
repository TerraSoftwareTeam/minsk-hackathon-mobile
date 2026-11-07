package com.dev.lbsuhack.core.data.session

import com.dev.lbsuhack.core.data.KeyValueStorage
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionClearHook
import com.dev.lbsuhack.core.domain.session.SessionRepository
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.network.auth.TokenStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class DefaultSessionRepository(
    private val storage: KeyValueStorage,
    private val tokenStorage: TokenStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clearHooks: () -> List<SessionClearHook> = { emptyList() },
) : SessionRepository {

    init {
        storage.remove(LEGACY_FATHERS_NAME_KEY)
    }

    private val mutex = Mutex()
    private val _state = MutableStateFlow<SessionState>(SessionState.Unknown)
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    private var restored = false

    override suspend fun restore() {
        withContext(ioDispatcher) {
            mutex.withLock {
                if (restored) return@withLock

                val id = storage.getString(KEY_ID)
                val name = storage.getString(KEY_NAME)
                val surname = storage.getString(KEY_SURNAME)
                val schoolsById = storage.getString(KEY_SCHOOLS_BY_ID)
                val schoolName = storage.getString(KEY_SCHOOL_NAME)
                val roleStr = storage.getString(KEY_ROLE)
                val role = roleStr?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
                    ?: UserRole.PUPIL
                val pupilClass = storage.getString(KEY_CLASS)

                _state.value = if (!id.isNullOrBlank() && !name.isNullOrBlank() && !tokenStorage.getAccessToken().isNullOrBlank()) {
                    SessionState.Authenticated(
                        SessionUser(
                            id = id,
                            name = name,
                            surname = surname,
                            schoolsById = schoolsById,
                            schoolName = schoolName,
                            role = role,
                            pupilClass = pupilClass,
                        ),
                    )
                } else {
                    SessionState.Unauthenticated
                }
                restored = true
            }
        }
    }

    override suspend fun save(user: SessionUser) {
        withContext(ioDispatcher) {
            mutex.withLock {
                storage.putString(KEY_ID, user.id)
                storage.putString(KEY_NAME, user.name)
                storage.putString(KEY_SURNAME, user.surname)
                storage.putString(KEY_SCHOOLS_BY_ID, user.schoolsById)
                storage.putString(KEY_SCHOOL_NAME, user.schoolName)
                storage.putString(KEY_ROLE, user.role.name)
                storage.putString(KEY_CLASS, user.pupilClass)

                _state.value = SessionState.Authenticated(user)
                restored = true
            }
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) {
            if (_state.value is SessionState.Authenticated) {
                clearHooks().forEach { hook ->
                    withTimeoutOrNull(CLEAR_HOOK_TIMEOUT_MS) { hook.beforeClear() }
                }
            }
            mutex.withLock {
                storage.clear()
                tokenStorage.clear()
                _state.value = SessionState.Unauthenticated
                restored = true
            }
        }
    }

    private companion object {
        const val CLEAR_HOOK_TIMEOUT_MS = 3_000L
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
