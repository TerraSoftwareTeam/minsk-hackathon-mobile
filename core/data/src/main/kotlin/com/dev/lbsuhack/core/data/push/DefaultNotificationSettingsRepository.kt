package com.dev.lbsuhack.core.data.push

import com.dev.lbsuhack.core.data.KeyValueStorage
import com.dev.lbsuhack.core.domain.push.NotificationSettingsRepository
import com.dev.lbsuhack.core.domain.push.PushTokenProvider
import com.dev.lbsuhack.core.domain.push.PushTokenRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class DefaultNotificationSettingsRepository(
    private val storage: KeyValueStorage,
    private val pushTokenProvider: PushTokenProvider,
    private val pushTokenRepository: PushTokenRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NotificationSettingsRepository {

    private val enabledFlow = MutableStateFlow(readStored())

    override val isEnabled: Flow<Boolean> = enabledFlow.asStateFlow()

    override suspend fun setNotificationsEnabled(enabled: Boolean): Boolean = withContext(ioDispatcher) {
        val token = pushTokenProvider.currentToken()?.takeIf(String::isNotBlank)
            ?: return@withContext false
        val delivered = if (enabled) {
            pushTokenRepository.register(token)
        } else {
            pushTokenRepository.unregister(token)
        }
        if (delivered) {
            storage.putString(KEY_NOTIFICATIONS_ENABLED, enabled.toString())
            enabledFlow.value = enabled
        }
        delivered
    }

    private fun readStored(): Boolean = storage.getString(KEY_NOTIFICATIONS_ENABLED)?.toBooleanStrictOrNull() ?: true

    private companion object {
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}
