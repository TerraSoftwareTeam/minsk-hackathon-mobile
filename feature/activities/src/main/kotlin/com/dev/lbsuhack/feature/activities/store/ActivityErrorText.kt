package com.dev.lbsuhack.feature.activities.store

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.R

internal enum class ActivityOperation {
    CREATE,
    VOTE,
    LIKE,
    DETAILS,
    LIST,
}

internal fun DomainError.toUiText(operation: ActivityOperation): UiText = when (this) {
    is DomainError.BusinessRule -> message?.let { UiText.Raw(it) } ?: UiText.Resource(R.string.activity_error_unknown)
    DomainError.Forbidden -> UiText.Resource(
        when (operation) {
            ActivityOperation.CREATE -> R.string.activity_error_forbidden_create
            ActivityOperation.VOTE -> R.string.activity_error_forbidden_vote
            ActivityOperation.LIKE -> R.string.activity_error_forbidden_like
            ActivityOperation.DETAILS -> R.string.activity_error_forbidden_details
            ActivityOperation.LIST -> R.string.activity_error_forbidden_list
        },
    )
    DomainError.Unauthorized -> UiText.Resource(R.string.activity_error_unauthorized)
    DomainError.Network, DomainError.Timeout -> UiText.Resource(R.string.activity_error_network)
    DomainError.NotFound -> UiText.Resource(R.string.activity_error_not_found)
    DomainError.Validation -> UiText.Resource(R.string.activity_error_validation)
    DomainError.Unknown -> UiText.Resource(R.string.activity_error_unknown)
    is DomainError.Exception -> UiText.Resource(R.string.activity_error_unknown)
}
