package com.dev.lbsuhack.feature.requests.navigation

import androidx.navigation3.runtime.NavKey
import com.dev.lbsuhack.core.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RequestsRoute(
    val role: UserRole,
) : NavKey
