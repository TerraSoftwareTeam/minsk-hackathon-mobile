package com.dev.lbsuhack.feature.auth

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.auth.data.AuthRepositoryImpl

internal fun DomainError.toUiText(): UiText = when (this) {
    DomainError.Validation -> UiText.Resource(R.string.auth_error_validation)
    DomainError.Network, DomainError.Timeout -> UiText.Resource(R.string.auth_error_network)
    DomainError.Unauthorized -> UiText.Resource(R.string.auth_error_credentials)
    DomainError.Forbidden -> UiText.Resource(R.string.auth_error_forbidden)
    DomainError.NotFound -> UiText.Resource(R.string.auth_error_unknown)
    DomainError.Unknown -> UiText.Resource(R.string.auth_error_unknown)
    is DomainError.BusinessRule -> when (code) {
        AuthRepositoryImpl.TEACHER_NOT_REGISTERED -> UiText.Resource(R.string.auth_error_teacher_not_registered)
        AuthRepositoryImpl.ROLE_MISMATCH -> UiText.Resource(roleMismatchText(message))
        else -> message?.let { UiText.Raw(it) } ?: UiText.Resource(R.string.auth_error_unknown)
    }
    is DomainError.Exception -> throwable.message?.let { UiText.Raw(it) } ?: UiText.Resource(R.string.auth_error_unknown)
}

private fun roleMismatchText(actualRole: String?): Int = when (actualRole) {
    UserRole.TEACHER.name -> R.string.auth_error_role_mismatch_teacher
    UserRole.ORGANIZATOR.name -> R.string.auth_error_role_mismatch_organizator
    else -> R.string.auth_error_role_mismatch_pupil
}
