package com.dev.lbsuhack.feature.requests.store.requests

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.toUiText
import com.dev.lbsuhack.feature.requests.domain.MAX_DECLINE_REASON_LENGTH
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewRequestRepository
import com.dev.lbsuhack.feature.requests.domain.isPending
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class RequestsStoreFactory(
    private val storeFactory: StoreFactory,
    private val requestRepository: ReviewRequestRepository,
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val pollIntervalMs: Long = DEFAULT_POLL_INTERVAL_MS,
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
        data class AccessResolved(val audience: RequestAudience?, val accessState: AccessState) : Message
        data object RefreshStarted : Message
        data object RefreshCompleted : Message
        data object RefreshFailed : Message
        data class RequestsLoaded(val requests: List<ReviewRequest>) : Message
        data class RequestUpdated(val request: ReviewRequest) : Message
        data class TabSelected(val tab: RequestsTab) : Message
        data class DialogOpened(val dialog: RequestDialog?) : Message
        data class DeclineReasonChanged(val value: String) : Message
        data class StartsAtChanged(val value: OffsetDateTime, val hasDateError: Boolean) : Message
        data object ChanceLoading : Message
        data class ChanceLoaded(val requestId: String, val chance: Double?) : Message
        data object Submitting : Message
        data object ActionCompleted : Message
        data object ActionFailed : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<RequestsStore.Intent, Nothing, RequestsStore.State, Message, RequestsStore.Label>() {

        private var requestedRole: UserRole? = null
        private var refreshedAudience: RequestAudience? = null
        private var sessionJob: Job? = null
        private var requestsJob: Job? = null
        private var pollingJob: Job? = null
        private var chanceJob: Job? = null

        override fun executeIntent(intent: RequestsStore.Intent) {
            when (intent) {
                is RequestsStore.Intent.Load -> {
                    requestedRole = intent.role
                    refreshedAudience = null
                    startObserving()
                    resolveAccess(observeSessionUseCase.state.value)
                }

                RequestsStore.Intent.Refresh -> refresh(state().audience)
                RequestsStore.Intent.Pause -> stopObserving()
                RequestsStore.Intent.StartPolling -> startPolling()
                RequestsStore.Intent.StopPolling -> stopPolling()
                is RequestsStore.Intent.SelectTab -> dispatch(Message.TabSelected(intent.tab))
                is RequestsStore.Intent.ClickApprove -> openApproveDialog(intent.requestId)
                is RequestsStore.Intent.ClickDecline -> openDeclineDialog(intent.requestId)
                RequestsStore.Intent.DismissDialog -> dismissDialog()
                is RequestsStore.Intent.ChangeDeclineReason -> {
                    dispatch(Message.DeclineReasonChanged(intent.value.take(MAX_DECLINE_REASON_LENGTH)))
                }

                is RequestsStore.Intent.ChangeStartsAt -> {
                    dispatch(Message.StartsAtChanged(intent.value, !isFutureDate(intent.value)))
                    loadApproveChance()
                }

                RequestsStore.Intent.ConfirmApprove -> confirmApprove()
                RequestsStore.Intent.ConfirmDecline -> confirmDecline()
            }
        }

        private fun startObserving() {
            if (sessionJob == null) {
                sessionJob = scope.launch {
                    observeSessionUseCase.state.collectLatest { sessionState ->
                        resolveAccess(sessionState)
                    }
                }
            }
            if (requestsJob == null) {
                requestsJob = scope.launch {
                    requestRepository.observeRequests().collectLatest { requests ->
                        dispatch(Message.RequestsLoaded(requests))
                    }
                }
            }
        }

        private fun stopObserving() {
            stopPolling()
            sessionJob?.cancel()
            sessionJob = null
            requestsJob?.cancel()
            requestsJob = null
        }

        private fun startPolling() {
            if (pollingJob?.isActive == true) return
            pollingJob = scope.launch {
                while (isActive) {
                    delay(pollIntervalMs)
                    val audience = state().audience ?: continue
                    if (state().isRefreshing || state().isSubmitting) continue
                    pollOnce(audience)
                }
            }
        }

        private fun stopPolling() {
            pollingJob?.cancel()
            pollingJob = null
        }

        private suspend fun pollOnce(audience: RequestAudience) {
            val knownPendingIds = state().queueRequests.filter { it.isPending }.mapTo(HashSet()) { it.id }
            val hadLoaded = state().hasLoadedOnce
            if (requestRepository.refresh(audience) !is DomainResult.Success) return
            val loaded = requestRepository.observeRequests().first()
            dispatch(Message.RequestsLoaded(loaded))
            dispatch(Message.RefreshCompleted)
            val arrived = loaded
                .count { it.audience == audience && it.isPending && it.id !in knownPendingIds }
            if (hadLoaded && arrived > 0) {
                publish(RequestsStore.Label.ShowMessage(UiText.Plural(R.plurals.requests_new_arrived, arrived)))
            }
        }

        private fun resolveAccess(sessionState: SessionState) {
            val audience = resolveAudience(sessionState)
            dispatch(Message.AccessResolved(audience, resolveAccessState(sessionState, audience)))
            if (audience != null && audience != refreshedAudience) {
                refreshedAudience = audience
                refresh(audience)
            }
        }

        private fun resolveAudience(sessionState: SessionState): RequestAudience? {
            val role = (sessionState as? SessionState.Authenticated)?.user?.role ?: return null
            if (role != requestedRole) return null
            return role.toRequestAudience()
        }

        private fun resolveAccessState(sessionState: SessionState, audience: RequestAudience?): AccessState = when {
            audience != null -> AccessState.Granted
            sessionState is SessionState.Unknown -> AccessState.Checking
            else -> AccessState.Denied
        }

        private fun refresh(audience: RequestAudience?) {
            if (audience == null) return
            dispatch(Message.RefreshStarted)
            scope.launch {
                when (val result = requestRepository.refresh(audience)) {
                    is DomainResult.Success -> {
                        dispatch(Message.RequestsLoaded(requestRepository.observeRequests().first()))
                        dispatch(Message.RefreshCompleted)
                    }
                    is DomainResult.Failure -> {
                        dispatch(Message.RefreshFailed)
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
                        chance = request.successProbability,
                    ),
                ),
            )
            if (state().audience == RequestAudience.ADMIN) loadApproveChance()
        }

        private fun loadApproveChance() {
            val dialog = state().dialog as? RequestDialog.Approve ?: return
            val request = state().queueRequests.find { it.id == dialog.requestId } ?: return
            val startsAt = dialog.startsAt?.takeIf { isFutureDate(it) } ?: return
            chanceJob?.cancel()
            dispatch(Message.ChanceLoading)
            chanceJob = scope.launch {
                delay(CHANCE_DEBOUNCE_MS)
                val result = requestRepository.previewSuccessProbability(
                    categoryKey = request.categoryKey,
                    startsAt = startsAt.toInstant().toString(),
                )
                val chance = (result as? DomainResult.Success)?.value ?: request.successProbability
                dispatch(Message.ChanceLoaded(dialog.requestId, chance))
            }
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

        private fun dismissDialog() {
            if (state().isSubmitting) return
            dispatch(Message.DialogOpened(null))
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
                val startsAt = dialog.startsAt?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                when (val result = requestRepository.approve(dialog.requestId, audience, startsAt)) {
                    is DomainResult.Success -> {
                        dispatch(Message.ActionCompleted)
                        dispatch(Message.RequestUpdated(result.value))
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
                        dispatch(Message.RequestUpdated(result.value))
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
            is Message.AccessResolved -> copy(audience = msg.audience, accessState = msg.accessState)
            Message.RefreshStarted -> copy(isRefreshing = true, loadFailed = false)
            Message.RefreshCompleted -> copy(isRefreshing = false, hasLoadedOnce = true)
            Message.RefreshFailed -> copy(isRefreshing = false, loadFailed = true)
            is Message.RequestsLoaded -> copy(
                requests = msg.requests,
                hasLoadedOnce = hasLoadedOnce || msg.requests.isNotEmpty(),
            )

            is Message.RequestUpdated -> copy(
                requests = requests.map { request ->
                    if (request.id == msg.request.id) msg.request else request
                },
            )

            is Message.TabSelected -> copy(selectedTab = msg.tab)
            is Message.DialogOpened -> copy(dialog = msg.dialog)
            is Message.DeclineReasonChanged -> copy(
                dialog = (dialog as? RequestDialog.Decline)?.copy(reason = msg.value) ?: dialog,
            )

            Message.ChanceLoading -> copy(
                dialog = (dialog as? RequestDialog.Approve)?.copy(isChanceLoading = true) ?: dialog,
            )

            is Message.ChanceLoaded -> copy(
                dialog = (dialog as? RequestDialog.Approve)
                    ?.takeIf { it.requestId == msg.requestId }
                    ?.copy(chance = msg.chance, isChanceLoading = false)
                    ?: dialog,
            )

            is Message.StartsAtChanged -> copy(
                dialog = (dialog as? RequestDialog.Approve)?.copy(
                    startsAt = msg.value,
                    hasDateError = msg.hasDateError,
                ) ?: dialog,
            )

            Message.Submitting -> copy(isSubmitting = true)
            Message.ActionCompleted -> copy(isSubmitting = false, dialog = null)
            Message.ActionFailed -> copy(isSubmitting = false)
        }
    }
}

internal fun defaultStartsAt(now: OffsetDateTime = OffsetDateTime.now()): OffsetDateTime =
    now.plusDays(DEFAULT_START_OFFSET_DAYS).withHour(DEFAULT_START_HOUR).withMinute(0).withSecond(0).withNano(0)

internal fun isFutureDate(startsAt: OffsetDateTime?, now: OffsetDateTime = OffsetDateTime.now()): Boolean =
    startsAt?.isAfter(now) == true

private const val DEFAULT_POLL_INTERVAL_MS = 10_000L
private const val CHANCE_DEBOUNCE_MS = 250L
private const val DEFAULT_START_OFFSET_DAYS = 7L
private const val DEFAULT_START_HOUR = 12
