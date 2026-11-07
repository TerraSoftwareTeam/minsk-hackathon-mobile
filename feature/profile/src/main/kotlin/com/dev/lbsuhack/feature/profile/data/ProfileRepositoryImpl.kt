package com.dev.lbsuhack.feature.profile.data

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow

class ProfileRepositoryImpl(
    private val userProfileApi: UserProfileApi,
) : ProfileRepository {

    private val savedItems = MutableStateFlow(0)

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
