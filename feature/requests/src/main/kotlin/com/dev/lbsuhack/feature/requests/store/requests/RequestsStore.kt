package com.dev.lbsuhack.feature.requests.store.requests

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.isPending
import com.dev.lbsuhack.feature.requests.domain.isProcessed

enum class RequestsTab {
    PENDING,
    PROCESSED,
    ALL,
}

sealed interface RequestDialog {
    val requestId: String
    val title: String

    data class Approve(
        override val requestId: String,
        override val title: String,
    ) : RequestDialog

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

private val reviewerRoles = setOf(UserRole.TEACHER, UserRole.ORGANIZATOR)

private fun isReviewer(role: UserRole?): Boolean =
    role != null && role in reviewerRoles

interface RequestsStore : Store<RequestsStore.Intent, RequestsStore.State, RequestsStore.Label> {

    sealed interface Intent {
        data class Load(val role: UserRole) : Intent
        data class SelectTab(val tab: RequestsTab) : Intent
        data class ClickApprove(val requestId: String) : Intent
        data class ClickDecline(val requestId: String) : Intent
        data object DismissDialog : Intent
        data class ChangeDeclineReason(val value: String) : Intent
        data object ConfirmApprove : Intent
        data object ConfirmDecline : Intent
    }

    data class State(
        val currentUser: SessionUser? = null,
        val audience: RequestAudience? = null,
        val requests: List<ReviewRequest> = emptyList(),
        val selectedTab: RequestsTab = RequestsTab.PENDING,
        val dialog: RequestDialog? = null,
        val isLoading: Boolean = true,
        val isSubmitting: Boolean = false,
    ) {
        val canReview: Boolean
            get() = audience != null && isReviewer(currentUser?.role)

        val queueRequests: List<ReviewRequest>
            get() {
                if (!canReview) return emptyList()
                val queueAudience = audience ?: return emptyList()
                val queue = requests.filter { it.audience == queueAudience }
                val teacherClass = currentUser?.pupilClass
                return if (queueAudience == RequestAudience.TEACHER && !teacherClass.isNullOrBlank()) {
                    queue.filter { it.author.pupilClass == teacherClass }
                } else {
                    queue
                }
            }

        val visibleRequests: List<ReviewRequest>
            get() = queueRequests.filter { request ->
                when (selectedTab) {
                    RequestsTab.PENDING -> request.isPending
                    RequestsTab.PROCESSED -> request.isProcessed
                    RequestsTab.ALL -> true
                }
            }

        val pendingCount: Int
            get() = queueRequests.count { it.isPending }
    }

    sealed interface Label {
        data class ShowMessage(val text: UiText) : Label
    }
}
