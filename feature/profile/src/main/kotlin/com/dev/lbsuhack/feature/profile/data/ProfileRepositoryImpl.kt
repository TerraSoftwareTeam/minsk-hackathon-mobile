package com.dev.lbsuhack.feature.profile.data

import android.content.Context
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow

interface ProfilePreferences {
    fun isNotificationsEnabled(): Boolean
    fun setNotificationsEnabled(enabled: Boolean)
}

internal class DefaultProfilePreferences(context: Context) : ProfilePreferences {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    override fun isNotificationsEnabled(): Boolean =
        preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    override fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    private companion object {
        const val PREFS_NAME = "profile_settings"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}

class ProfileRepositoryImpl internal constructor(
    private val preferences: ProfilePreferences,
    private val userProfileApi: UserProfileApi,
) : ProfileRepository {

    constructor(
        context: Context,
        userProfileApi: UserProfileApi,
    ) : this(
        preferences = DefaultProfilePreferences(context),
        userProfileApi = userProfileApi,
    )

    private val savedItems = MutableStateFlow(0)

    override suspend fun isNotificationsEnabled(): Boolean =
        preferences.isNotificationsEnabled()

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferences.setNotificationsEnabled(enabled)
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
}
