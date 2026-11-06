package com.dev.lbsuhack.notification

import com.dev.lbsuhack.core.domain.push.RegisterPushTokenUseCase
import com.dev.lbsuhack.core.domain.push.UnregisterPushTokenUseCase
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionClearHook
import com.dev.lbsuhack.core.domain.session.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PushTokenSync(
    private val registerPushToken: RegisterPushTokenUseCase,
    private val unregisterPushToken: UnregisterPushTokenUseCase,
    private val observeSession: ObserveSessionUseCase,
    private val scope: CoroutineScope,
) : SessionClearHook {

    fun start() {
        scope.launch {
            observeSession.state
                .map { (it as? SessionState.Authenticated)?.user?.id }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { currentToken()?.let { token -> registerPushToken(token) } }
        }
    }

    fun onNewToken(token: String) {
        if (observeSession.state.value !is SessionState.Authenticated) return
        scope.launch { registerPushToken(token) }
    }

    override suspend fun beforeClear() {
        currentToken()?.let { token -> unregisterPushToken(token) }
    }

    private suspend fun currentToken(): String? = suspendCancellableCoroutine { continuation ->
        runCatching {
            KolaNotificationManager.fetchPushToken { token ->
                if (continuation.isActive) continuation.resume(token?.takeIf(String::isNotBlank))
            }
        }.onFailure {
            if (continuation.isActive) continuation.resume(null)
        }
    }
}
