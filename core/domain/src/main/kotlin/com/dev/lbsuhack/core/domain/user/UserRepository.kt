package com.dev.lbsuhack.core.domain.user

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    val currentUser: StateFlow<User?>

    suspend fun getCurrentUser(forceRefresh: Boolean = false): DomainResult<User>

    suspend fun updateCache(user: User)

    suspend fun clearCache()
}
