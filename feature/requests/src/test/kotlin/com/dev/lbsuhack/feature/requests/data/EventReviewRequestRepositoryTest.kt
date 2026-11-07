package com.dev.lbsuhack.feature.requests.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.StandardTestDispatcher
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.feature.requests.util.FakeSessionRepository
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.network.client.AppHttpClientFactory
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EventReviewRequestRepositoryTest {

    private val json = AppHttpClientFactory.defaultJson

    private fun eventJson(
        id: String,
        status: String,
        declineReason: String? = null,
        startsAt: String? = "2026-11-21T10:00:00.000Z",
    ): String = """
        {
          "id": "$id",
          "name": "Экскурсия в музей",
          "description": "Описание",
          "scope": "CLASS",
          "category": "CULTURAL",
          "status": "$status",
          "successProbability": 0.5, "imageUrl": null, "ratingAverage": null, "ratingsCount": 0,
                                "declineReason": ${declineReason?.let { "\"$it\"" } ?: "null"},
          "voteThreshold": 15,
          "startsAt": ${startsAt?.let { "\"$it\"" } ?: "null"},
          "votingClosesAt": "2026-11-19T10:00:00.000Z",
          "initiator": {
            "id": "pupil-1",
            "schoolsById": "school-1",
            "name": "Иван",
            "surname": "Иванов",
            "class": "10A",
            "role": "PUPIL"
          },
          "createdByRole": "PUPIL", "moderators": [{"name": "Петр", "surname": "Петров"}],
          "createdAt": "2026-11-06T00:00:00.000Z",
          "updatedAt": "2026-11-06T00:00:00.000Z",
          "voters": [{"name": "Пётр", "surname": "Петров"}], "votesCount": 1, "likesCount": 0
        }
    """.trimIndent()

    private fun MockRequestHandleScope.okResponse(content: String): HttpResponseData = respond(
        content = content,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )

    private fun createRepository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): EventReviewRequestRepository {
        val engine = MockEngine { request -> handler(request) }
        val client = HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }
        return EventReviewRequestRepository(client)
    }

    @Test
    fun logoutInvalidatesCachedRequestsAndInFlightRefreshForTheSameAccount() = runTest {
        val user = SessionUser("teacher", "First")
        val session = FakeSessionRepository(SessionState.Authenticated(user))
        val gate = CompletableDeferred<Unit>()
        var delayed = false
        HttpClient(MockEngine {
            if (delayed) gate.await()
            okResponse("[${eventJson("e-1", "ON_MODERATION")}]")
        }) {
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = EventReviewRequestRepository(client, StandardTestDispatcher(testScheduler), ObserveSessionUseCase(session))
            repository.refresh(RequestAudience.TEACHER)
            assertEquals(1, repository.observeRequests().first().size)
            delayed = true
            val pending = async { repository.refresh(RequestAudience.TEACHER) }
            runCurrent()
            repository.beforeClear()
            session.clear()
            session.save(user)
            assertTrue(repository.observeRequests().first().isEmpty())
            gate.complete(Unit)
            assertEquals(DomainError.Unauthorized, (pending.await() as DomainResult.Failure).error)
            assertTrue(repository.observeRequests().first().isEmpty())
            delayed = false
            repository.refresh(RequestAudience.TEACHER)
            assertEquals(1, repository.observeRequests().first().size)
            session.save(user.copy(id = "second"))
            assertTrue(repository.observeRequests().first().isEmpty())
        }
    }

    @Test
    fun refreshTeacherLoadsModerationList() = runTest {
        val repository = createRepository { request ->
            when (request.url.encodedPath) {
                "/api/v1/events/moderation" -> okResponse("[${eventJson("e-1", "ON_MODERATION")}]")
                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }

        val result = repository.refresh(RequestAudience.TEACHER)
        val requests = repository.observeRequests().firstValue()

        assertTrue(result is DomainResult.Success)
        assertEquals(1, requests.size)
        val request = requests.first()
        assertEquals("e-1", request.id)
        assertEquals("Экскурсия в музей", request.title)
        assertEquals("Описание", request.description)
        assertEquals("Иван Иванов", request.authorName)
        assertEquals(listOf("Пётр Петров"), request.voters)
        assertEquals(1, request.votesCount)
        assertEquals(RequestAudience.TEACHER, request.audience)
        assertEquals(ReviewStatus.PENDING_TEACHER, request.status)
        assertEquals(OffsetDateTime.parse("2026-11-21T10:00:00Z"), request.proposedDate)
    }

    @Test
    fun refreshAdminLoadsAdminModerationList() = runTest {
        val repository = createRepository { request ->
            when (request.url.encodedPath) {
                "/api/v1/events/admin-moderation" -> okResponse("[${eventJson("e-2", "ON_ADMIN_MODERATION")}]")
                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }

        val result = repository.refresh(RequestAudience.ADMIN)
        val requests = repository.observeRequests().firstValue()

        assertTrue(result is DomainResult.Success)
        assertEquals(RequestAudience.ADMIN, requests.first().audience)
        assertEquals(ReviewStatus.PENDING_ADMIN, requests.first().status)
    }

    @Test
    fun brokenStartsAtDoesNotBreakTheMapping() = runTest {
        val repository = createRepository { request ->
            when (request.url.encodedPath) {
                "/api/v1/events/moderation" ->
                    okResponse("[${eventJson("e-1", "ON_MODERATION", startsAt = "не дата")}]")

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }

        repository.refresh(RequestAudience.TEACHER)
        val request = repository.observeRequests().firstValue().first()

        assertNull(request.proposedDate)
    }

    @Test
    fun approveTeacherPostsToApproveEndpoint() = runTest {
        val repository = createRepository { request ->
            when {
                request.url.encodedPath == "/api/v1/events/moderation" ->
                    okResponse("[${eventJson("e-1", "ON_MODERATION")}]")

                request.method == HttpMethod.Post &&
                    request.url.encodedPath == "/api/v1/events/e-1/approve" ->
                    okResponse(eventJson("e-1", "ON_VOTE"))

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }
        repository.refresh(RequestAudience.TEACHER)

        val result = repository.approve("e-1", RequestAudience.TEACHER)

        assertTrue(result is DomainResult.Success)
        val updated = (result as DomainResult.Success).value
        assertEquals(ReviewStatus.APPROVED, updated.status)
        assertEquals(ReviewStatus.APPROVED, repository.observeRequests().firstValue().first().status)
    }

    @Test
    fun refreshKeepsRequestsProcessedInThisSession() = runTest {
        var moderationList = "[${eventJson("e-1", "ON_MODERATION")}]"
        val repository = createRepository { request ->
            when {
                request.url.encodedPath == "/api/v1/events/moderation" -> okResponse(moderationList)

                request.method == HttpMethod.Post &&
                    request.url.encodedPath == "/api/v1/events/e-1/approve" ->
                    okResponse(eventJson("e-1", "ON_VOTE"))

                request.method == HttpMethod.Get && request.url.encodedPath == "/api/v1/events/e-1" ->
                    okResponse(eventJson("e-1", "EXPIRED"))

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }
        repository.refresh(RequestAudience.TEACHER)
        repository.approve("e-1", RequestAudience.TEACHER)
        moderationList = "[${eventJson("e-2", "ON_MODERATION")}]"

        repository.refresh(RequestAudience.TEACHER)
        val requests = repository.observeRequests().firstValue()

        assertEquals(listOf("e-2", "e-1"), requests.map { it.id })
        assertEquals(ReviewStatus.APPROVED, requests.last().status)
    }

    @Test
    fun refreshDropsProcessedRequestsDeletedOnServer() = runTest {
        var moderationList = "[${eventJson("e-1", "ON_MODERATION")}]"
        val repository = createRepository { request ->
            when {
                request.url.encodedPath == "/api/v1/events/moderation" -> okResponse(moderationList)

                request.method == HttpMethod.Post &&
                    request.url.encodedPath == "/api/v1/events/e-1/approve" ->
                    okResponse(eventJson("e-1", "ON_VOTE"))

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }
        repository.refresh(RequestAudience.TEACHER)
        repository.approve("e-1", RequestAudience.TEACHER)
        moderationList = "[]"

        repository.refresh(RequestAudience.TEACHER)

        assertTrue(repository.observeRequests().firstValue().isEmpty())
    }

    @Test
    fun approveAdminSendsNoBody() = runTest {
        var approveBody: OutgoingContent? = null
        val repository = createRepository { request ->
            when {
                request.url.encodedPath == "/api/v1/events/admin-moderation" ->
                    okResponse("[${eventJson("e-2", "ON_ADMIN_MODERATION")}]")

                request.method == HttpMethod.Post &&
                    request.url.encodedPath == "/api/v1/events/e-2/admin/approve" -> {
                    approveBody = request.body
                    okResponse(eventJson("e-2", "FINALIZED"))
                }

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }
        repository.refresh(RequestAudience.ADMIN)

        val result = repository.approve("e-2", RequestAudience.ADMIN)

        assertTrue(approveBody is OutgoingContent.NoContent)
        assertTrue(result is DomainResult.Success)
        assertEquals(ReviewStatus.APPROVED, (result as DomainResult.Success).value.status)
    }

    @Test
    fun declineTeacherSendsReasonBody() = runTest {
        var declineRequestBody: OutgoingContent? = null
        val repository = createRepository { request ->
            when {
                request.url.encodedPath == "/api/v1/events/moderation" ->
                    okResponse("[${eventJson("e-1", "ON_MODERATION")}]")

                request.url.encodedPath == "/api/v1/events/e-1/decline" -> {
                    declineRequestBody = request.body
                    okResponse(eventJson("e-1", "DECLINED", declineReason = "Слишком поздно"))
                }

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }
        repository.refresh(RequestAudience.TEACHER)

        val result = repository.decline("e-1", "Слишком поздно", RequestAudience.TEACHER)

        assertTrue(declineRequestBody !is OutgoingContent.NoContent)
        assertTrue(result is DomainResult.Success)
        val updated = (result as DomainResult.Success).value
        assertEquals(ReviewStatus.DECLINED, updated.status)
        assertEquals("Слишком поздно", updated.declineReason)
    }

    @Test
    fun declineWithoutReasonSendsNoBody() = runTest {
        var declineRequestBody: OutgoingContent? = null
        val repository = createRepository { request ->
            when {
                request.url.encodedPath == "/api/v1/events/moderation" ->
                    okResponse("[${eventJson("e-1", "ON_MODERATION")}]")

                request.url.encodedPath == "/api/v1/events/e-1/decline" -> {
                    declineRequestBody = request.body
                    okResponse(eventJson("e-1", "DECLINED"))
                }

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }
        repository.refresh(RequestAudience.TEACHER)

        val result = repository.decline("e-1", null, RequestAudience.TEACHER)

        assertTrue(declineRequestBody is OutgoingContent.NoContent)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun declineAdminPostsToAdminDeclineEndpoint() = runTest {
        val repository = createRepository { request ->
            when {
                request.url.encodedPath == "/api/v1/events/admin-moderation" ->
                    okResponse("[${eventJson("e-2", "ON_ADMIN_MODERATION")}]")

                request.method == HttpMethod.Post &&
                    request.url.encodedPath == "/api/v1/events/e-2/admin/decline" ->
                    okResponse(eventJson("e-2", "DECLINED_BY_ADMIN", declineReason = "Нет мест"))

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }
        repository.refresh(RequestAudience.ADMIN)

        val result = repository.decline("e-2", "Нет мест", RequestAudience.ADMIN)

        assertTrue(result is DomainResult.Success)
        val updated = (result as DomainResult.Success).value
        assertEquals(ReviewStatus.DECLINED, updated.status)
        assertEquals("Нет мест", updated.declineReason)
    }

    @Test
    fun requestIdIsEncodedInTheActionPath() = runTest {
        val requestedPath = mutableListOf<String>()
        val repository = createRepository { request ->
            requestedPath += request.url.encodedPath
            when {
                request.url.encodedPath == "/api/v1/events/moderation" ->
                    okResponse("[${eventJson("e 1", "ON_MODERATION")}]")

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }
        repository.refresh(RequestAudience.TEACHER)

        repository.approve("e 1", RequestAudience.TEACHER)

        assertTrue(requestedPath.any { it.contains("e%201") })
    }

    @Test
    fun forbiddenRefreshReturnsFailure() = runTest {
        val repository = createRepository { _ ->
            respond("Forbidden", HttpStatusCode.Forbidden)
        }

        val result = repository.refresh(RequestAudience.TEACHER)

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Forbidden, (result as DomainResult.Failure).error)
    }

    private suspend fun Flow<List<ReviewRequest>>.firstValue(): List<ReviewRequest> = first()
}
