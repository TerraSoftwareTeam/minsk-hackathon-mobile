package com.dev.lbsuhack.feature.profile.data

import android.content.Context
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow

class ProfileRepositoryImpl(
    context: Context,
    private val userProfileApi: UserProfileApi,
) : ProfileRepository {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val savedItems = MutableStateFlow(0)

    override suspend fun isNotificationsEnabled(): Boolean =
        preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    override suspend fun savedCount(): Int = savedItems.value

    override suspend fun updateOwnProfile(
        userId: String,
        name: String,
        surname: String,
    ): DomainResult<User> = userProfileApi.updateProfile(
        userId = userId,
        name = name,
        surname = surname,
    )

    private companion object {
        const val PREFS_NAME = "profile_settings"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}
