package com.dev.lbsuhack.feature.activities

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.localization.text.UiText

internal fun DomainError.toUiText(): UiText = when (this) {
    is DomainError.BusinessRule -> message?.let { UiText.Raw(it) } ?: UiText.Resource(R.string.create_activity_error_unknown)
    DomainError.Forbidden -> UiText.Resource(R.string.create_activity_error_forbidden)
    DomainError.Unauthorized -> UiText.Resource(R.string.create_activity_error_unauthorized)
    DomainError.Network, DomainError.Timeout -> UiText.Resource(R.string.create_activity_error_network)
    DomainError.NotFound -> UiText.Resource(R.string.create_activity_error_not_found)
    DomainError.Validation -> UiText.Resource(R.string.create_activity_error_validation)
    DomainError.Unknown -> UiText.Resource(R.string.create_activity_error_unknown)
    is DomainError.Exception -> throwable.message?.let { UiText.Raw(it) } ?: UiText.Resource(R.string.create_activity_error_unknown)
}
