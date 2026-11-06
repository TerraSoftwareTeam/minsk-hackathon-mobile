package com.dev.lbsuhack.feature.profile.domain

interface ProfileRepository {

    suspend fun isNotificationsEnabled(): Boolean

    suspend fun setNotificationsEnabled(enabled: Boolean)

    suspend fun savedCount(): Int
}
