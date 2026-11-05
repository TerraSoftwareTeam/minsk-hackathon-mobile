package com.dev.lbsuhack.feature.requests.store.requests

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewRequestRepository
import com.dev.lbsuhack.feature.requests.domain.isPending
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
        data object Loading : Message
        data class AudienceResolved(val audience: RequestAudience?) : Message
        data class UserUpdated(val user: SessionUser?) : Message
        data class RequestsLoaded(val requests: List<ReviewRequest>) : Message
        data class TabSelected(val tab: RequestsTab) : Message
        data class DialogOpened(val dialog: RequestDialog?) : Message
        data class ConfirmedDateChanged(val value: String) : Message
        data class ConfirmedLocationChanged(val value: String) : Message
        data class DeclineReasonChanged(val value: String) : Message
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
                    dispatch(Message.AudienceResolved(intent.role.toRequestAudience()))
                    startObserving()
                }
                is RequestsStore.Intent.SelectTab -> dispatch(Message.TabSelected(intent.tab))
                is RequestsStore.Intent.ClickApprove -> openApproveDialog(intent.requestId)
                is RequestsStore.Intent.ClickDecline -> openDeclineDialog(intent.requestId)
                RequestsStore.Intent.DismissDialog -> dispatch(Message.DialogOpened(null))
                is RequestsStore.Intent.ChangeConfirmedDate -> dispatch(Message.ConfirmedDateChanged(intent.value))
                is RequestsStore.Intent.ChangeConfirmedLocation -> dispatch(Message.ConfirmedLocationChanged(intent.value))
                is RequestsStore.Intent.ChangeDeclineReason -> dispatch(Message.DeclineReasonChanged(intent.value))
                RequestsStore.Intent.ConfirmApprove -> confirmApprove()
                RequestsStore.Intent.ConfirmDecline -> confirmDecline()
            }
        }

        private fun startObserving() {
            if (observing) return
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
        }

        private fun openApproveDialog(requestId: String) {
            val request = state().queueRequests.find { it.id == requestId } ?: return
            if (!request.isPending) return

            val dialog = when (request) {
                is ReviewRequest.Publication -> RequestDialog.Approve(
                    requestId = request.id,
                    title = request.title,
                    requiresDetails = false,
                )

                is ReviewRequest.Visit -> RequestDialog.Approve(
                    requestId = request.id,
                    title = request.title,
                    confirmedDate = request.proposedDate,
                    confirmedLocation = request.proposedLocation,
                    requiresDetails = true,
                )
            }
            dispatch(Message.DialogOpened(dialog))
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

            val request = state().queueRequests.find { it.id == dialog.requestId } ?: return
            if (request is ReviewRequest.Visit && (dialog.confirmedDate.isBlank() || dialog.confirmedLocation.isBlank())) {
                publish(RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_error_details_required)))
                return
            }

            dispatch(Message.Submitting)
            scope.launch {
                try {
                    when (request) {
                        is ReviewRequest.Publication -> requestRepository.approvePublication(request.id)
                        is ReviewRequest.Visit -> requestRepository.approveVisit(
                            requestId = request.id,
                            confirmedDate = dialog.confirmedDate,
                            confirmedLocation = dialog.confirmedLocation,
                        )
                    }
                    dispatch(Message.ActionCompleted)
                    publish(RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_success_approved)))
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    dispatch(Message.ActionFailed)
                    publish(RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_error_action_failed)))
                }
            }
        }

        private fun confirmDecline() {
            val dialog = state().dialog as? RequestDialog.Decline ?: return
            if (state().isSubmitting) return

            val request = state().queueRequests.find { it.id == dialog.requestId } ?: return

            dispatch(Message.Submitting)
            scope.launch {
                try {
                    requestRepository.decline(
                        requestId = request.id,
                        reason = dialog.reason,
                    )
                    dispatch(Message.ActionCompleted)
                    publish(RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_success_declined)))
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    dispatch(Message.ActionFailed)
                    publish(RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_error_action_failed)))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RequestsStore.State, Message> {
        override fun RequestsStore.State.reduce(msg: Message): RequestsStore.State = when (msg) {
            Message.Loading -> copy(isLoading = true)
            is Message.AudienceResolved -> copy(audience = msg.audience)
            is Message.UserUpdated -> copy(currentUser = msg.user)
            is Message.RequestsLoaded -> copy(requests = msg.requests, isLoading = false)
            is Message.TabSelected -> copy(selectedTab = msg.tab)
            is Message.DialogOpened -> copy(dialog = msg.dialog)
            is Message.ConfirmedDateChanged -> copy(
                dialog = (dialog as? RequestDialog.Approve)?.copy(confirmedDate = msg.value),
            )
            is Message.ConfirmedLocationChanged -> copy(
                dialog = (dialog as? RequestDialog.Approve)?.copy(confirmedLocation = msg.value),
            )
            is Message.DeclineReasonChanged -> copy(
                dialog = (dialog as? RequestDialog.Decline)?.copy(reason = msg.value),
            )
            Message.Submitting -> copy(isSubmitting = true)
            Message.ActionCompleted -> copy(isSubmitting = false, dialog = null)
            Message.ActionFailed -> copy(isSubmitting = false)
        }
    }
}
