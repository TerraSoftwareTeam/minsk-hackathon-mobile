package com.dev.lbsuhack.feature.activities.store

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation

internal fun DomainError.toUiText(operation: ActivityOperation): UiText = when (this) {
    is DomainError.BusinessRule -> UiText.Resource(
        when (code) {
            HTTP_CONFLICT -> when (operation) {
                ActivityOperation.CREATE -> R.string.activity_error_create_conflict
                ActivityOperation.VOTE -> R.string.activity_error_vote_conflict
                ActivityOperation.DELETE -> R.string.activity_error_delete_conflict
                else -> R.string.activity_error_state_changed
            }
            HTTP_FORBIDDEN -> when (operation) {
                ActivityOperation.DELETE -> R.string.activity_error_forbidden_delete
                else -> R.string.activity_error_forbidden_details
            }
            HTTP_BAD_REQUEST -> R.string.activity_error_validation
            else -> R.string.activity_error_unknown
        },
    )
    DomainError.Forbidden -> UiText.Resource(
        when (operation) {
            ActivityOperation.CREATE -> R.string.activity_error_forbidden_create
            ActivityOperation.VOTE -> R.string.activity_error_forbidden_vote
            ActivityOperation.LIKE -> R.string.activity_error_forbidden_like
            ActivityOperation.DETAILS -> R.string.activity_error_forbidden_details
            ActivityOperation.LIST -> R.string.activity_error_forbidden_list
            ActivityOperation.DELETE -> R.string.activity_error_forbidden_delete
        },
    )
    DomainError.Unauthorized -> UiText.Resource(R.string.activity_error_unauthorized)
    DomainError.Network, DomainError.Timeout -> UiText.Resource(R.string.activity_error_network)
    DomainError.NotFound -> UiText.Resource(R.string.activity_error_not_found)
    DomainError.Validation -> UiText.Resource(R.string.activity_error_validation)
    DomainError.Unknown -> UiText.Resource(R.string.activity_error_unknown)
    is DomainError.Exception -> UiText.Resource(R.string.activity_error_unknown)
}

private const val HTTP_BAD_REQUEST = "400"
private const val HTTP_FORBIDDEN = "403"
private const val HTTP_CONFLICT = "409"
