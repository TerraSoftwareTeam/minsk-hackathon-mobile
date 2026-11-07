package com.dev.lbsuhack.notification

import com.dev.lbsuhack.core.domain.push.PushTokenProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class KolaPushTokenProvider : PushTokenProvider {

    override suspend fun currentToken(): String? = suspendCancellableCoroutine { continuation ->
        runCatching {
            KolaNotificationManager.fetchPushToken { token ->
                if (continuation.isActive) continuation.resume(token?.takeIf(String::isNotBlank))
            }
        }.onFailure {
            if (continuation.isActive) continuation.resume(null)
        }
    }
}
