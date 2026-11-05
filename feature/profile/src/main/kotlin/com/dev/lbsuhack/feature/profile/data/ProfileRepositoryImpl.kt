package com.dev.lbsuhack.feature.profile.data

import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class ProfileRepositoryImpl : ProfileRepository {

    private val notificationsEnabled = MutableStateFlow(true)
    private val savedItems = MutableStateFlow(0)

    override suspend fun isNotificationsEnabled(): Boolean = notificationsEnabled.value

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled.value = enabled
    }

    override suspend fun savedCount(): Int = savedItems.value

    override suspend fun incrementSaved(): Int {
        savedItems.update { it + 1 }
        return savedItems.value
    }
}
