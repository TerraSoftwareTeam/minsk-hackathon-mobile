package com.dev.lbsuhack.feature.requests.store.requests

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.toUiText
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewRequestRepository
import com.dev.lbsuhack.feature.requests.domain.isPending
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class RequestsStoreFactory(
    private val storeFactory: StoreFactory,
    private val requestRepository: ReviewRequestRepository,
    private val observeSessionUseCase: ObserveSessionUseCase,
) {

    fun create(): RequestsStore =
        object : RequestsStore,
            Store<RequestsStore.Intent, RequestsStore.State, RequestsStore.Label> by storeFactory.create(
                name = "RequestsStore",
                initialState = RequestsStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data object Refreshed : Message
        data object Loading : Message
        data class AudienceResolved(val audience: RequestAudience?) : Message
        data class UserUpdated(val user: SessionUser?) : Message
        data class RequestsLoaded(val requests: List<ReviewRequest>) : Message
        data object RequestsLoadFailed : Message
        data class TabSelected(val tab: RequestsTab) : Message
        data class DialogOpened(val dialog: RequestDialog?) : Message
        data class DeclineReasonChanged(val value: String) : Message
        data class StartsAtChanged(val value: String) : Message
        data object Submitting : Message
        data object ActionCompleted : Message
        data object ActionFailed : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<RequestsStore.Intent, Nothing, RequestsStore.State, Message, RequestsStore.Label>() {

        private var observing = false

        override fun executeIntent(intent: RequestsStore.Intent) {
            when (intent) {
                is RequestsStore.Intent.Load -> {
                    val actualRole = (observeSessionUseCase.state.value as? SessionState.Authenticated)?.user?.role
                    val audience = if (actualRole == intent.role) intent.role.toRequestAudience() else null
                    dispatch(Message.AudienceResolved(audience))
                    startObserving(audience)
                }

                is RequestsStore.Intent.SelectTab -> dispatch(Message.TabSelected(intent.tab))
                is RequestsStore.Intent.ClickApprove -> openApproveDialog(intent.requestId)
                is RequestsStore.Intent.ClickDecline -> openDeclineDialog(intent.requestId)
                RequestsStore.Intent.DismissDialog -> dispatch(Message.DialogOpened(null))
                is RequestsStore.Intent.ChangeDeclineReason -> dispatch(Message.DeclineReasonChanged(intent.value))
                is RequestsStore.Intent.ChangeStartsAt -> dispatch(Message.StartsAtChanged(intent.value))
                RequestsStore.Intent.ConfirmApprove -> confirmApprove()
                RequestsStore.Intent.ConfirmDecline -> confirmDecline()
            }
        }

        private fun startObserving(audience: RequestAudience?) {
            if (observing) {
                refresh(audience)
                return
            }
            observing = true

            scope.launch {
                observeSessionUseCase.state.collectLatest { sessionState ->
                    val user = (sessionState as? SessionState.Authenticated)?.user
                    dispatch(Message.UserUpdated(user))
                }
            }

            scope.launch {
                dispatch(Message.Loading)
                requestRepository.observeRequests().collectLatest { requests ->
                    dispatch(Message.RequestsLoaded(requests))
                }
            }

            refresh(audience)
        }

        private fun refresh(audience: RequestAudience?) {
            if (audience == null) return
            dispatch(Message.Loading)
            scope.launch {
                when (val result = requestRepository.refresh(audience)) {
                    is DomainResult.Success -> dispatch(Message.Refreshed)
                    is DomainResult.Failure -> {
                        dispatch(Message.RequestsLoadFailed)
                        publish(RequestsStore.Label.ShowMessage(result.error.toUiText()))
                    }
                }
            }
        }

        private fun openApproveDialog(requestId: String) {
            val request = state().queueRequests.find { it.id == requestId } ?: return
            if (!request.isPending) return

            dispatch(
                Message.DialogOpened(
                    RequestDialog.Approve(
                        requestId = request.id,
                        title = request.title,
                        startsAt = if (state().audience == RequestAudience.ADMIN) defaultStartsAt() else null,
                    ),
                ),
            )
        }

        private fun openDeclineDialog(requestId: String) {
            val request = state().queueRequests.find { it.id == requestId } ?: return
            if (!request.isPending) return

            dispatch(
                Message.DialogOpened(
                    RequestDialog.Decline(
                        requestId = request.id,
                        title = request.title,
                    ),
                ),
            )
        }

        private fun confirmApprove() {
            val dialog = state().dialog as? RequestDialog.Approve ?: return
            if (state().isSubmitting) return
            val audience = state().audience ?: return
            if (audience == RequestAudience.ADMIN && !isFutureDate(dialog.startsAt)) {
                publish(RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_error_date_in_past)))
                return
            }

            dispatch(Message.Submitting)
            scope.launch {
                when (val result = requestRepository.approve(dialog.requestId, audience, dialog.startsAt)) {
                    is DomainResult.Success -> {
                        dispatch(Message.ActionCompleted)
                        publish(RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_success_approved)))
                    }

                    is DomainResult.Failure -> {
                        dispatch(Message.ActionFailed)
                        publish(RequestsStore.Label.ShowMessage(result.error.toUiText()))
                    }
                }
            }
        }

        private fun confirmDecline() {
            val dialog = state().dialog as? RequestDialog.Decline ?: return
            if (state().isSubmitting) return
            val audience = state().audience ?: return

            dispatch(Message.Submitting)
            scope.launch {
                when (
                    val result = requestRepository.decline(
                        requestId = dialog.requestId,
                        reason = dialog.reason,
                        audience = audience,
                    )
                ) {
                    is DomainResult.Success -> {
                        dispatch(Message.ActionCompleted)
                        publish(RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_success_declined)))
                    }

                    is DomainResult.Failure -> {
                        dispatch(Message.ActionFailed)
                        publish(RequestsStore.Label.ShowMessage(result.error.toUiText()))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RequestsStore.State, Message> {
        override fun RequestsStore.State.reduce(msg: Message): RequestsStore.State = when (msg) {
            Message.Refreshed -> copy(isLoading = false)
            Message.Loading -> copy(isLoading = true)
            is Message.AudienceResolved -> copy(audience = msg.audience)
            is Message.UserUpdated -> copy(currentUser = msg.user)
            is Message.RequestsLoaded -> copy(requests = msg.requests, isLoading = false)
            Message.RequestsLoadFailed -> copy(isLoading = false)
            is Message.TabSelected -> copy(selectedTab = msg.tab)
            is Message.DialogOpened -> copy(dialog = msg.dialog)
            is Message.DeclineReasonChanged -> copy(
                dialog = (dialog as? RequestDialog.Decline)?.copy(reason = msg.value),
            )
            is Message.StartsAtChanged -> copy(
                dialog = (dialog as? RequestDialog.Approve)?.copy(startsAt = msg.value) ?: dialog,
            )
            Message.Submitting -> copy(isSubmitting = true)
            Message.ActionCompleted -> copy(isSubmitting = false, dialog = null)
            Message.ActionFailed -> copy(isSubmitting = false)
        }
    }
}

internal fun defaultStartsAt(now: OffsetDateTime = OffsetDateTime.now()): String =
    now.plusDays(DEFAULT_START_OFFSET_DAYS).withHour(DEFAULT_START_HOUR).withMinute(0).withSecond(0).withNano(0)
        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

internal fun isFutureDate(startsAt: String?, now: OffsetDateTime = OffsetDateTime.now()): Boolean =
    startsAt?.let { runCatching { OffsetDateTime.parse(it).isAfter(now) }.getOrNull() } == true

private const val DEFAULT_START_OFFSET_DAYS = 7L
private const val DEFAULT_START_HOUR = 12
