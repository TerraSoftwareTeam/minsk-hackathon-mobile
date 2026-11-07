package com.dev.lbsuhack.feature.requests

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.localization.text.UiText

internal fun DomainError.toUiText(): UiText = when (this) {
    DomainError.Network, DomainError.Timeout -> UiText.Resource(R.string.requests_error_network)
    DomainError.NotFound -> UiText.Resource(R.string.requests_error_not_found)
    DomainError.Unauthorized, DomainError.Forbidden -> UiText.Resource(R.string.requests_error_forbidden)
    DomainError.Validation -> UiText.Resource(R.string.requests_error_validation)
    DomainError.Unknown -> UiText.Resource(R.string.requests_error_unknown)
    is DomainError.BusinessRule -> when (code) {
        HTTP_BAD_REQUEST -> UiText.Resource(R.string.requests_error_validation)
        HTTP_NOT_FOUND -> UiText.Resource(R.string.requests_error_not_found)
        HTTP_CONFLICT -> UiText.Resource(R.string.requests_error_conflict)
        else -> UiText.Resource(R.string.requests_error_unknown)
    }

    is DomainError.Exception -> UiText.Resource(R.string.requests_error_unknown)
}

private const val HTTP_BAD_REQUEST = "400"
private const val HTTP_NOT_FOUND = "404"
private const val HTTP_CONFLICT = "409"
