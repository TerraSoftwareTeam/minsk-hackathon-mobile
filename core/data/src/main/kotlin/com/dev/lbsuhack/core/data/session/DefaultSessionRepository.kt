package com.dev.lbsuhack.core.data.session

import android.content.Context
import com.dev.lbsuhack.core.domain.session.SessionRepository
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SessionRepository {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private val mutex = Mutex()
    private val _state = MutableStateFlow<SessionState>(SessionState.Unknown)
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    private var restored = false

    override suspend fun restore() {
        withContext(ioDispatcher) {
            mutex.withLock {
                if (restored) return@withLock

                val id = preferences.getLong(KEY_ID, NO_ID)
                val name = preferences.getString(KEY_NAME, null)
                val email = preferences.getString(KEY_EMAIL, null)

                _state.value = if (id != NO_ID && !name.isNullOrBlank() && !email.isNullOrBlank()) {
                    SessionState.Authenticated(
                        SessionUser(id = id, name = name, email = email),
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
                    .putLong(KEY_ID, user.id)
                    .putString(KEY_NAME, user.name)
                    .putString(KEY_EMAIL, user.email)
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
                _state.value = SessionState.Unauthenticated
                restored = true
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "user_session"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val NO_ID = -1L
    }
}
