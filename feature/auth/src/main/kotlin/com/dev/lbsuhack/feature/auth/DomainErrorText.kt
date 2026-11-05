package com.dev.lbsuhack.feature.auth

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.localization.text.UiText

internal fun DomainError.toUiText(): UiText = when (this) {
    DomainError.Validation -> UiText.Resource(R.string.auth_error_validation)
    DomainError.Network, DomainError.Timeout -> UiText.Resource(R.string.auth_error_network)
    DomainError.Unauthorized -> UiText.Resource(R.string.auth_error_credentials)
    DomainError.Forbidden -> UiText.Resource(R.string.auth_error_forbidden)
    DomainError.NotFound -> UiText.Resource(R.string.auth_error_unknown)
    DomainError.Unknown -> UiText.Resource(R.string.auth_error_unknown)
    is DomainError.BusinessRule -> UiText.Resource(R.string.auth_error_unknown)
    is DomainError.Exception -> UiText.Resource(R.string.auth_error_unknown)
}
