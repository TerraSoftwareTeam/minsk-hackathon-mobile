package com.dev.lbsuhack.feature.tournaments

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.localization.text.UiText

internal fun DomainError.toUiText(): UiText = when (this) {
    DomainError.Network, DomainError.Timeout -> UiText.Resource(R.string.tournaments_error_network)
    DomainError.NotFound -> UiText.Resource(R.string.tournaments_error_not_found)
    DomainError.Unauthorized -> UiText.Resource(R.string.tournaments_error_unauthorized)
    DomainError.Forbidden -> UiText.Resource(R.string.tournaments_error_forbidden)
    DomainError.Validation -> UiText.Resource(R.string.tournaments_error_unknown)
    DomainError.Unknown -> UiText.Resource(R.string.tournaments_error_unknown)
    is DomainError.BusinessRule -> UiText.Resource(R.string.tournaments_error_unknown)
    is DomainError.Exception -> UiText.Resource(R.string.tournaments_error_unknown)
}
