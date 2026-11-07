package com.dev.lbsuhack.feature.requests.store.requests

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.isPending
import com.dev.lbsuhack.feature.requests.domain.isProcessed

enum class RequestsSectionKind {
    PENDING,
    PROCESSED,
}

data class RequestsSection(
    val kind: RequestsSectionKind,
    val requests: List<ReviewRequest>,
)

enum class RequestsTab {
    PENDING,
    PROCESSED,
    ALL,
}

sealed interface AccessState {
    data object Checking : AccessState
    data object Granted : AccessState
    data object Denied : AccessState
}

sealed interface RequestDialog {
    val requestId: String
    val title: String

    data class Decline(
        override val requestId: String,
        override val title: String,
        val reason: String = "",
    ) : RequestDialog
}

internal fun UserRole.toRequestAudience(): RequestAudience? = when (this) {
    UserRole.PUPIL -> null
    UserRole.TEACHER -> RequestAudience.TEACHER
    UserRole.ORGANIZATOR -> RequestAudience.ADMIN
}

interface RequestsStore : Store<RequestsStore.Intent, RequestsStore.State, RequestsStore.Label> {

    sealed interface Intent {
        data class Load(val role: UserRole) : Intent
        data object Refresh : Intent
        data object Pause : Intent
        data object StartPolling : Intent
        data object StopPolling : Intent
        data class SelectTab(val tab: RequestsTab) : Intent
        data class ClickApprove(val requestId: String) : Intent
        data class ClickDecline(val requestId: String) : Intent
        data object DismissDialog : Intent
        data class ChangeDeclineReason(val value: String) : Intent
        data object ConfirmDecline : Intent
    }

    data class State(
        val audience: RequestAudience? = null,
        val userName: String = "",
        val userClass: String? = null,
        val accessState: AccessState = AccessState.Checking,
        val requests: List<ReviewRequest> = emptyList(),
        val selectedTab: RequestsTab = RequestsTab.PENDING,
        val dialog: RequestDialog? = null,
        val isRefreshing: Boolean = false,
        val hasLoadedOnce: Boolean = false,
        val loadFailed: Boolean = false,
        val isSubmitting: Boolean = false,
        val approvingRequestId: String? = null,
    ) {
        val canReview: Boolean
            get() = accessState == AccessState.Granted

        val queueRequests: List<ReviewRequest>
            get() {
                if (!canReview) return emptyList()
                val queueAudience = audience ?: return emptyList()
                return requests.filter { it.audience == queueAudience }
            }

        val visibleRequests: List<ReviewRequest>
            get() = queueRequests.filter { request ->
                when (selectedTab) {
                    RequestsTab.PENDING -> request.isPending
                    RequestsTab.PROCESSED -> request.isProcessed
                    RequestsTab.ALL -> true
                }
            }

        val sections: List<RequestsSection>
            get() = listOf(
                RequestsSection(RequestsSectionKind.PENDING, visibleRequests.filter { it.isPending }),
                RequestsSection(RequestsSectionKind.PROCESSED, visibleRequests.filter { it.isProcessed }),
            ).filter { it.requests.isNotEmpty() }

        val queueSections: List<RequestsSection>
            get() = listOf(
                RequestsSection(RequestsSectionKind.PENDING, queueRequests.filter { it.isPending }),
                RequestsSection(RequestsSectionKind.PROCESSED, queueRequests.filter { it.isProcessed }),
            ).filter { it.requests.isNotEmpty() }

        val pendingCount: Int
            get() = queueRequests.count { it.isPending }

        val isInitialLoading: Boolean
            get() = !hasLoadedOnce && !loadFailed
    }

    sealed interface Label {
        data class ShowMessage(val text: UiText) : Label
    }
}
