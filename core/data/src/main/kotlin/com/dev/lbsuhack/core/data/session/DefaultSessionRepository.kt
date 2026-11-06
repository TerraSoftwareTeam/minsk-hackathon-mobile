package com.dev.lbsuhack.core.data.session

import android.content.Context
import com.dev.lbsuhack.core.domain.model.UserRole
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

class DefaultSessionRepository(
    context: Context,
    private val tokenStorage: TokenStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SessionRepository {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    ).apply {
        edit().remove("fathers_name").apply()
    }

    private val mutex = Mutex()
    private val _state = MutableStateFlow<SessionState>(SessionState.Unknown)
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    private var restored = false

    override suspend fun restore() {
        withContext(ioDispatcher) {
            mutex.withLock {
                if (restored) return@withLock

                val id = preferences.getString(KEY_ID, null)
                val name = preferences.getString(KEY_NAME, null)
                val surname = preferences.getString(KEY_SURNAME, null)
                val schoolsById = preferences.getString(KEY_SCHOOLS_BY_ID, null)
                val schoolName = preferences.getString(KEY_SCHOOL_NAME, null)
                val roleStr = preferences.getString(KEY_ROLE, null)
                val role = roleStr?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
                    ?: UserRole.PUPIL
                val pupilClass = preferences.getString(KEY_CLASS, null)

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
                preferences.edit()
                    .putString(KEY_ID, user.id)
                    .putString(KEY_NAME, user.name)
                    .putString(KEY_SURNAME, user.surname)
                    .putString(KEY_SCHOOLS_BY_ID, user.schoolsById)
                    .putString(KEY_SCHOOL_NAME, user.schoolName)
                    .putString(KEY_ROLE, user.role.name)
                    .putString(KEY_CLASS, user.pupilClass)
                    .apply()

                _state.value = SessionState.Authenticated(user)
                restored = true
            }
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) {
            mutex.withLock {
                preferences.edit().clear().apply()
                tokenStorage.clear()
                _state.value = SessionState.Unauthenticated
                restored = true
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "user_session"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_SURNAME = "surname"
        const val KEY_SCHOOLS_BY_ID = "schools_by_id"
        const val KEY_SCHOOL_NAME = "school_name"
        const val KEY_ROLE = "role"
        const val KEY_CLASS = "pupil_class"
    }
}
