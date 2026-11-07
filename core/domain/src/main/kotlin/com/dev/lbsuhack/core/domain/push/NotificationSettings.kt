package com.dev.lbsuhack.core.domain.push

import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {

    val isEnabled: Flow<Boolean>

    suspend fun setNotificationsEnabled(enabled: Boolean): Boolean
}

class ObserveNotificationsEnabledUseCase(
    private val repository: NotificationSettingsRepository,
) {

    operator fun invoke(): Flow<Boolean> = repository.isEnabled
}

class SetNotificationsEnabledUseCase(
    private val repository: NotificationSettingsRepository,
) {

    suspend operator fun invoke(enabled: Boolean): Boolean = repository.setNotificationsEnabled(enabled)
}
