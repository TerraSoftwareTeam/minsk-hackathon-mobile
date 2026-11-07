package com.dev.lbsuhack.feature.profile.domain

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User

interface ProfileRepository {

    suspend fun savedCount(): Int

    suspend fun updateOwnProfile(
        userId: String,
        name: String,
        surname: String,
    ): DomainResult<User>
}
