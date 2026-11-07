package com.dev.lbsuhack.feature.activities.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runCurrent
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.feature.activities.util.FakeSessionRepository
import org.junit.Assert.assertFalse
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import com.dev.lbsuhack.core.network.client.AppHttpClientFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ActivityRepositoryImplTest {

    private val json = AppHttpClientFactory.defaultJson

    @Test
    fun reactionEndpointsPreserveNegativeScoresAndPendingReactionDuringRefresh() = runTest {
        val event = """
            {
              "id": "e-sync", "name": "Синхронизация", "description": null, "scope": "CLASS", "category": "SPORTS",
              "status": "ON_VOTE", "declineReason": null, "voteThreshold": 10, "startsAt": null,
              "votingClosesAt": "2026-12-01T00:00:00.000Z", "successProbability": 0.5, "imageUrl": null,
              "likesCount": 0, "initiator": {"name": "А", "surname": "Б"}, "createdByRole": "PUPIL", "moderators": [],
              "voters": [], "votesCount": 0, "createdAt": "2026-11-01T00:00:00.000Z",
              "updatedAt": "2026-11-01T00:00:00.000Z", "hasVoted": false, "likedByMe": false
            }
        """.trimIndent()
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val calls = mutableListOf<String>()
        var serverReaction = 0
        var delayDislike = false
        var rejectNextLike = false
        HttpClient(MockEngine(io.ktor.client.engine.mock.MockEngineConfig().apply {
            dispatcher = StandardTestDispatcher(testScheduler)
            addHandler { request ->
                val path = request.url.encodedPath
                if (path.endsWith("/like") && rejectNextLike) {
                    rejectNextLike = false
                    return@addHandler respond("{}", HttpStatusCode.InternalServerError, headersOf(HttpHeaders.ContentType, "application/json"))
                }
                val body = when (path) {
                    "/api/v1/events/voting" -> "[" + event
                        .replace("\"likesCount\": 0", "\"likesCount\": $serverReaction")
                        .replace("\"likedByMe\": false", "\"likedByMe\": ${serverReaction == 1}, \"dislikedByMe\": ${serverReaction == -1}") + "]"
                    "/api/v1/events/my" -> "[]"
                    else -> {
                        calls += "${request.method.value} $path"
                        if (path.endsWith("/dislike") && delayDislike) gate.await()
                        serverReaction = when {
                            request.method.value == "DELETE" -> 0
                            path.endsWith("/dislike") -> -1
                            else -> 1
                        }
                        """{"eventId":"e-sync","liked":${serverReaction == 1},"disliked":${serverReaction == -1},"likesCount":$serverReaction}"""
                    }
                }
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        })) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = ActivityRepositoryImpl(client, StandardTestDispatcher(testScheduler))
            repository.refresh()
            val first = (repository.dislike("e-sync") as DomainResult.Success).value
            assertEquals(-1, first.likesCount)
            assertTrue(first.dislikedByMe)
            advanceUntilIdle()
            repository.refresh()
            assertTrue(repository.observeActivities().first().single().dislikedByMe)
            assertEquals(-1, repository.observeActivities().first().single().likesCount)

            assertEquals(1, (repository.like("e-sync") as DomainResult.Success).value.likesCount)
            advanceUntilIdle()
            assertEquals(0, (repository.unlike("e-sync") as DomainResult.Success).value.likesCount)
            advanceUntilIdle()
            delayDislike = true
            repository.dislike("e-sync")
            runCurrent()
            repository.refresh()
            assertEquals(-1, repository.observeActivities().first().single().likesCount)
            gate.complete(Unit)
            advanceUntilIdle()
            val final = repository.observeActivities().first().single()
            assertEquals(-1, final.likesCount)
            assertTrue(final.dislikedByMe)
            assertFalse(final.likedByMe)
            rejectNextLike = true
            assertEquals(1, (repository.like("e-sync") as DomainResult.Success).value.likesCount)
            advanceUntilIdle()
            val rolledBack = repository.observeActivities().first().single()
            assertEquals(-1, rolledBack.likesCount)
            assertTrue(rolledBack.dislikedByMe)
            assertFalse(rolledBack.likedByMe)
        }
        assertTrue("PUT /api/v1/events/e-sync/dislike" in calls)
        assertTrue("PUT /api/v1/events/e-sync/like" in calls)
        assertTrue("DELETE /api/v1/events/e-sync/like" in calls)
    }

    @Test
    fun logoutInvalidatesCachedActivitiesAndInFlightRefreshForTheSameAccount() = runTest {
        val event = """
            {
              "id": "e-sync", "name": "Синхронизация", "description": null, "scope": "CLASS", "category": "SPORTS",
              "status": "ON_VOTE", "declineReason": null, "voteThreshold": 10, "startsAt": null,
              "votingClosesAt": "2026-12-01T00:00:00.000Z", "successProbability": 0.5, "imageUrl": null,
              "likesCount": 1, "initiator": {"name": "А", "surname": "Б"}, "createdByRole": "PUPIL", "moderators": [],
              "voters": [], "votesCount": 0, "createdAt": "2026-11-01T00:00:00.000Z",
              "updatedAt": "2026-11-01T00:00:00.000Z", "hasVoted": false, "likedByMe": false
            }
        """.trimIndent()
        val user = SessionUser("me", "First")
        val session = FakeSessionRepository(SessionState.Authenticated(user))
        val gate = CompletableDeferred<Unit>()
        var delayed = false
        HttpClient(MockEngine { request ->
            if (delayed) gate.await()
            respond(
                if (request.url.encodedPath.endsWith("/voting")) "[$event]" else "[]",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }) {
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = ActivityRepositoryImpl(client, StandardTestDispatcher(testScheduler), ObserveSessionUseCase(session))
            repository.refresh()
            assertEquals(1, repository.observeActivities().first().size)
            assertEquals("e-sync", repository.cachedEventById("e-sync")?.id)
            delayed = true
            val pending = async { repository.refresh() }
            runCurrent()
            repository.beforeClear()
            session.clear()
            session.save(user)
            assertTrue(repository.observeActivities().first().isEmpty())
            assertTrue(repository.cachedEventById("e-sync") == null)
            gate.complete(Unit)
            assertEquals(DomainError.Unauthorized, (pending.await() as DomainResult.Failure).error)
            assertTrue(repository.observeActivities().first().isEmpty())
            assertTrue(repository.cachedEventById("e-sync") == null)
            delayed = false
            repository.refresh()
            assertEquals(1, repository.observeActivities().first().size)
            assertEquals("e-sync", repository.cachedEventById("e-sync")?.id)
            session.save(user.copy(id = "second"))
            assertTrue(repository.observeActivities().first().isEmpty())
            assertTrue(repository.cachedEventById("e-sync") == null)
        }
    }

    @Test
    fun refreshLoadsVotingEvents() = runTest {
        val mockEngine = MockEngine { request ->
            val responseHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            when (request.url.encodedPath) {
                "/api/v1/events/voting" -> {
                    respond(
                        content = """
                            [
                              {
                                "id": "vote-1",
                                "name": "Турнир по волейболу",
                                "description": "В спортзале",
                                "scope": "SCHOOL",
                                "category": "SPORTS",
                                "status": "ON_VOTE",
                                "successProbability": 0.5, "imageUrl": null, "ratingAverage": null, "ratingsCount": 0,
                                "declineReason": null,
                                "voteThreshold": 15,
                                "startsAt": "2026-11-21T10:00:00.000Z",
                                "votingClosesAt": "2026-11-19T10:00:00.000Z",
                                "initiator": {
                                  "id": "1",
                                  "schoolsById": "school-1",
                                  "name": "Иван",
                                  "surname": "Иванов",
                                  "class": "10A",
                                  "role": "PUPIL"
                                },
                                "createdByRole": "PUPIL", "moderators": [{"name": "Петр", "surname": "Петров"}],
                                "createdAt": "2026-11-06T00:00:00.000Z",
                                "updatedAt": "2026-11-06T00:00:00.000Z",
                                "votesCount": 5,
                                "hasVoted": false,
                                "voters": [{"name": "Пётр", "surname": "Петров"}], "likesCount": 0,
                                "likedByMe": false
                              }
                            ]
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = responseHeaders,
                    )
                }

                "/api/v1/events/my" -> {
                    respond(
                        content = "[]",
                        status = HttpStatusCode.OK,
                        headers = responseHeaders,
                    )
                }

                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = ActivityRepositoryImpl(httpClient)
        val result = repository.refresh()

        assertTrue(result is com.dev.lbsuhack.core.domain.DomainResult.Success)
        val event = repository.getEventById("vote-1")
        assertTrue(event is com.dev.lbsuhack.core.domain.DomainResult.Success)
        assertEquals("Турнир по волейболу", (event as com.dev.lbsuhack.core.domain.DomainResult.Success).value.title)
        assertEquals(ActivityCategory.SPORTS, event.value.category)
        assertEquals(5, event.value.votesCount)
        assertEquals(listOf("Пётр Петров"), event.value.voters)
        assertEquals(15, event.value.voteThreshold)
    }

    @Test
    fun votePostsEventIdAndReturnsUpdated() = runTest {
        val mockEngine = MockEngine { request ->
            val responseHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            if (request.url.encodedPath == "/api/v1/events/vote-1") {
                respond(content = """{
                                "id": "vote-1",
                                "name": "Турнир по волейболу",
                                "description": "В спортзале",
                                "scope": "SCHOOL",
                                "category": "SPORTS",
                                "status": "ON_VOTE",
                                "successProbability": 0.5, "imageUrl": null, "ratingAverage": null, "ratingsCount": 0,
                                "declineReason": null,
                                "voteThreshold": 15,
                                "startsAt": "2026-11-21T10:00:00.000Z",
                                "votingClosesAt": "2026-11-19T10:00:00.000Z",
                                "initiator": {
                                  "id": "1",
                                  "schoolsById": "school-1",
                                  "name": "Иван",
                                  "surname": "Иванов",
                                  "class": "10A",
                                  "role": "PUPIL"
                                },
                                "createdByRole": "PUPIL", "moderators": [{"name": "Петр", "surname": "Петров"}],
                                "createdAt": "2026-11-06T00:00:00.000Z",
                                "updatedAt": "2026-11-06T00:00:00.000Z",
                                "voters": [{"name": "Пётр", "surname": "Петров"}], "likesCount": 0,
                                "votesCount": 5,
                                "hasVoted": false
                              }""", status = HttpStatusCode.OK, headers = responseHeaders)
            } else if (request.url.encodedPath == "/api/v1/events/vote-1/vote") {
                respond(
                    content = """
                        {
                          "eventId": "vote-1",
                          "votesCount": 6,
                          "voteThreshold": 15,
                          "status": "ON_VOTE"
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = responseHeaders,
                )
            } else {
                respond("Not found", HttpStatusCode.NotFound)
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = ActivityRepositoryImpl(httpClient)
        val result = repository.vote("vote-1")

        assertTrue(result is com.dev.lbsuhack.core.domain.DomainResult.Success)
        val proposal = (result as com.dev.lbsuhack.core.domain.DomainResult.Success).value
        assertEquals(6, proposal.votesCount)
        assertTrue(proposal.hasVoted)
    }

    @Test
    fun createEventPostsDtoAndReturnsEvent() = runTest {
        val mockEngine = MockEngine { request ->
            val responseHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            if (request.url.encodedPath == "/api/v1/events") {
                respond(
                    content = """
                        {
                          "id": "new-event-1",
                          "name": "Новое событие",
                          "description": "Описание",
                          "scope": "CLASS",
                          "category": "CULTURAL",
                          "status": "ON_MODERATION",
                          "successProbability": 0.5, "imageUrl": null, "ratingAverage": null, "ratingsCount": 0,
                                "declineReason": null,
                          "voteThreshold": 15,
                          "startsAt": "2026-11-21T10:00:00.000Z",
                          "votingClosesAt": "2026-11-19T10:00:00.000Z",
                          "initiator": {
                            "id": "1",
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
                    """.trimIndent(),
                    status = HttpStatusCode.Created,
                    headers = responseHeaders,
                )
            } else {
                respond("Not found", HttpStatusCode.NotFound)
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = ActivityRepositoryImpl(httpClient)
        val result = repository.createEvent(
            name = "Новое событие",
            category = ActivityCategory.CULTURAL,
            description = "Описание",
            scope = ActivityScope.CLASS,
            voteThreshold = 15,
            startsAt = "2026-12-05T12:00:00+03:00",
        )

        assertTrue(result is com.dev.lbsuhack.core.domain.DomainResult.Success)
        val proposal = (result as com.dev.lbsuhack.core.domain.DomainResult.Success).value
        assertEquals("new-event-1", proposal.id)
        assertEquals("Новое событие", proposal.title)
        assertEquals(ActivityCategory.CULTURAL, proposal.category)
    }
    @Test
    fun photoUploadUsesMultipartAndReturnsRemoteImage() = runTest {
        val fixture = """{
                                "id": "vote-1",
                                "name": "Турнир по волейболу",
                                "description": "В спортзале",
                                "scope": "SCHOOL",
                                "category": "SPORTS",
                                "status": "ON_VOTE",
                                "successProbability": 0.5, "imageUrl": "https://example.test/photo.jpg", "ratingAverage": null, "ratingsCount": 0,
                                "declineReason": null,
                                "voteThreshold": 15,
                                "startsAt": "2026-11-21T10:00:00.000Z",
                                "votingClosesAt": "2026-11-19T10:00:00.000Z",
                                "initiator": {
                                  "id": "1",
                                  "schoolsById": "school-1",
                                  "name": "Иван",
                                  "surname": "Иванов",
                                  "class": "10A",
                                  "role": "PUPIL"
                                },
                                "createdByRole": "PUPIL", "moderators": [{"name": "Петр", "surname": "Петров"}],
                                "createdAt": "2026-11-06T00:00:00.000Z",
                                "updatedAt": "2026-11-06T00:00:00.000Z",
                                "voters": [{"name": "Пётр", "surname": "Петров"}], "likesCount": 0,
                                "votesCount": 5,
                                "hasVoted": false
                              }"""
        HttpClient(MockEngine { request ->
            assertEquals(io.ktor.http.HttpMethod.Put, request.method)
            assertEquals("/api/v1/events/vote-1/image", request.url.encodedPath)
            assertTrue(request.body is io.ktor.client.request.forms.MultiPartFormDataContent)
            respond(fixture, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = ActivityRepositoryImpl(client, imageSource = ActivityImageSource {
                ActivityImage(byteArrayOf(1, 2, 3), "image/png")
            })
            val result = repository.uploadPhoto("vote-1", "content://photos/1")
            assertEquals("https://example.test/photo.jpg", (result as com.dev.lbsuhack.core.domain.DomainResult.Success).value.photoUri)
        }
    }

    @Test
    fun previewProbabilitySendsCategoryAndUtcStart() = runTest {
        var seen: io.ktor.http.Url? = null
        HttpClient(MockEngine { request ->
            seen = request.url
            respond("{\"successProbability\": 0.5461}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val result = ActivityRepositoryImpl(client).previewSuccessProbability(
                com.dev.lbsuhack.feature.activities.domain.ActivityCategory.SPORTS,
                java.time.OffsetDateTime.parse("2026-12-05T12:00:00+03:00"),
            )

            assertEquals(0.5461, (result as com.dev.lbsuhack.core.domain.DomainResult.Success).value, 0.00001)
            assertEquals("/api/v1/events/probability", seen?.encodedPath)
            assertEquals("SPORTS", seen?.parameters?.get("category"))
            assertEquals("2026-12-05T09:00:00Z", seen?.parameters?.get("startsAt"))
        }
    }

    @Test
    fun mainFeedUsesRoleAppropriateEndpoint() = runTest {
        val roles = mapOf(
            com.dev.lbsuhack.core.domain.model.UserRole.PUPIL to listOf("/api/v1/events/my", "/api/v1/events/voting"),
            com.dev.lbsuhack.core.domain.model.UserRole.TEACHER to listOf("/api/v1/events/my"),
            com.dev.lbsuhack.core.domain.model.UserRole.ORGANIZATOR to listOf("/api/v1/events/my"),
        )
        for ((role, path) in roles) {
            val requests = java.util.concurrent.CopyOnWriteArrayList<String>()
            val session = com.dev.lbsuhack.feature.activities.util.FakeSessionRepository(
                com.dev.lbsuhack.core.domain.session.SessionState.Authenticated(
                    com.dev.lbsuhack.core.domain.session.SessionUser("user", "Иван", role = role),
                ),
            )
            HttpClient(MockEngine { request ->
                requests += request.url.encodedPath
                respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }) {
                install(ContentNegotiation) { json(json) }
            }.use { client ->
                val repository = ActivityRepositoryImpl(client,
                    observeSession = com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase(session))
                assertTrue(repository.refresh() is com.dev.lbsuhack.core.domain.DomainResult.Success)
                assertEquals(path, requests.sorted())
            }
        }
    }

    @Test
    fun pupilFeedMergesVotingAndOwnEventsWithoutDuplicates() = runTest {
        fun event(id: String, status: String, votes: Int) = """
            {
              "id": "$id", "name": "Event $id", "description": null, "scope": "CLASS", "category": "SPORTS",
              "status": "$status", "declineReason": null, "voteThreshold": 10, "startsAt": null,
              "votingClosesAt": "2026-12-06T10:00:00.000Z", "successProbability": 0.5, "imageUrl": null,
              "voters": [{"name": "Пётр", "surname": "Петров"}], "votesCount": 1, "likesCount": 0,
              "initiator": {"id": "me", "schoolsById": "s-me", "name": "Иван", "surname": "Иванов", "class": "10A", "role": "PUPIL"},
              "createdByRole": "PUPIL", "moderators": [],
              "createdAt": "2026-11-06T10:00:00.000Z", "updatedAt": "2026-11-06T10:00:00.000Z",
              "votesCount": $votes, "hasVoted": false, "likedByMe": false
            }
        """.trimIndent()
        val session = com.dev.lbsuhack.feature.activities.util.FakeSessionRepository(
            com.dev.lbsuhack.core.domain.session.SessionState.Authenticated(
                com.dev.lbsuhack.core.domain.session.SessionUser(
                    "me", "Иван", role = com.dev.lbsuhack.core.domain.model.UserRole.PUPIL,
                ),
            ),
        )
        HttpClient(MockEngine { request ->
            val body = when (request.url.encodedPath) {
                "/api/v1/events/voting" -> "[${event("shared", "ON_VOTE", 3)}]"
                "/api/v1/events/my" -> "[${event("shared", "ON_VOTE", 3)}, ${event("mine", "ON_MODERATION", 0)}]"
                else -> error("unexpected ${request.url.encodedPath}")
            }
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = ActivityRepositoryImpl(
                client,
                observeSession = com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase(session),
            )
            assertTrue(repository.refresh() is com.dev.lbsuhack.core.domain.DomainResult.Success)
            val items = repository.observeActivities().first()
            assertEquals(listOf("shared", "mine"), items.map { it.id })
            assertTrue(items.all { it.isMine })
            assertEquals(
                com.dev.lbsuhack.feature.activities.domain.ActivityStatus.ON_MODERATION,
                items.last().status,
            )
        }
    }

    @Test
    fun startsAtIsFormattedAsConfirmedTime() {
        assertEquals(
            "5 декабря, 12:00",
            "2026-12-05T09:00:00.000Z".toDisplayDateTime(
                zone = java.time.ZoneId.of("Europe/Minsk"),
                locale = java.util.Locale.forLanguageTag("ru"),
            ),
        )
        assertEquals("not-a-date", "not-a-date".toDisplayDateTime())
    }

    @Test
    fun unvoteLikeAndUnlikeHitEndpointsAndUpdateEvent() = runTest {
        val calls = java.util.concurrent.CopyOnWriteArrayList<String>()
        val event = """
            {
              "id": "e1", "name": "Поход", "description": null, "scope": "CLASS", "category": "TOURISM",
              "status": "ON_VOTE", "declineReason": null, "voteThreshold": 10, "startsAt": null,
              "votingClosesAt": "2026-12-06T10:00:00.000Z", "successProbability": 0.5, "imageUrl": null,
              "likesCount": 2, "initiator": {"name": "Аня", "surname": "Смирнова"}, "createdByRole": "PUPIL", "moderators": [],
              "voters": [{"name": "Иван", "surname": "Иванов"}, {"name": "Пётр", "surname": "Петров"}],
              "votesCount": 2, "createdAt": "2026-11-06T10:00:00.000Z", "updatedAt": "2026-11-06T10:00:00.000Z",
              "hasVoted": true, "likedByMe": false
            }
        """.trimIndent()
        val session = com.dev.lbsuhack.feature.activities.util.FakeSessionRepository(
            com.dev.lbsuhack.core.domain.session.SessionState.Authenticated(
                com.dev.lbsuhack.core.domain.session.SessionUser(
                    "me", "Иван", surname = "Иванов", role = com.dev.lbsuhack.core.domain.model.UserRole.PUPIL,
                ),
            ),
        )
        HttpClient(MockEngine { request ->
            calls += "${request.method.value} ${request.url.encodedPath}"
            val body = when (request.url.encodedPath) {
                "/api/v1/events/voting" -> "[$event]"
                "/api/v1/events/my" -> "[]"
                "/api/v1/events/e1/vote" -> """{"eventId": "e1", "votesCount": 1, "voteThreshold": 10, "status": "ON_VOTE"}"""
                "/api/v1/events/e1/like" -> if (request.method.value == "PUT") {
                    """{"eventId": "e1", "liked": true, "likesCount": 3}"""
                } else {
                    """{"eventId": "e1", "liked": false, "likesCount": 2}"""
                }
                else -> error("unexpected ${request.url.encodedPath}")
            }
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = ActivityRepositoryImpl(
                client,
                observeSession = com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase(session),
                ioDispatcher = StandardTestDispatcher(testScheduler),
            )
            repository.refresh()
            assertTrue(!repository.observeActivities().first().single().isMine)

            val unvoted = (repository.unvote("e1") as com.dev.lbsuhack.core.domain.DomainResult.Success).value
            assertEquals(1, unvoted.votesCount)
            assertTrue(!unvoted.hasVoted)
            assertEquals(listOf("Пётр Петров"), unvoted.voters)

            val liked = (repository.like("e1") as com.dev.lbsuhack.core.domain.DomainResult.Success).value
            assertEquals(3, liked.likesCount)
            assertTrue(liked.likedByMe)

            val unliked = (repository.unlike("e1") as com.dev.lbsuhack.core.domain.DomainResult.Success).value
            assertEquals(2, unliked.likesCount)
            assertTrue(!unliked.likedByMe)
            advanceUntilIdle()
            assertEquals(1, repository.observeActivities().first().single().votesCount)
        }
        assertTrue("DELETE /api/v1/events/e1/vote" in calls)
        assertTrue("PUT /api/v1/events/e1/like" in calls)
        assertTrue("DELETE /api/v1/events/e1/like" in calls)
    }

    @Test
    fun getEventByIdReturnsForbiddenOn403() = runTest {
        HttpClient(MockEngine {
            respond(
                content = """{"message": "Forbidden"}""",
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = ActivityRepositoryImpl(client)
            val result = repository.getEventById("forbidden-id")
            assertTrue(result is com.dev.lbsuhack.core.domain.DomainResult.Failure)
            assertEquals(
                com.dev.lbsuhack.core.domain.DomainError.Forbidden,
                (result as com.dev.lbsuhack.core.domain.DomainResult.Failure).error,
            )
        }
    }

    @Test
    fun getEventByIdPreservesVoterAndInitiatorStateOnDirectFetch() = runTest {
        val eventJson = """
            {
              "id": "event-10", "name": "Олимпиада", "description": "По математике", "scope": "SCHOOL",
              "category": "INTELLECTUAL", "status": "ON_VOTE", "declineReason": null, "voteThreshold": 20,
              "startsAt": null, "votingClosesAt": "2026-12-01T00:00:00.000Z", "successProbability": 0.8,
              "imageUrl": null, "likesCount": 5, "initiator": {"name": "Анна", "surname": "Иванова"},
              "createdByRole": "PUPIL", "moderators": [], "voters": [{"name": "Анна", "surname": "Иванова"}, {"name": "Борис", "surname": "Петров"}],
              "votesCount": 2, "createdAt": "2026-11-01T00:00:00.000Z", "updatedAt": "2026-11-01T00:00:00.000Z"
            }
        """.trimIndent()
        val session = com.dev.lbsuhack.feature.activities.util.FakeSessionRepository(
            com.dev.lbsuhack.core.domain.session.SessionState.Authenticated(
                com.dev.lbsuhack.core.domain.session.SessionUser(
                    id = "user-1", name = "Анна", surname = "Иванова", role = com.dev.lbsuhack.core.domain.model.UserRole.PUPIL,
                ),
            ),
        )
        HttpClient(MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/events/voting" -> respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                "/api/v1/events/my" -> respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                "/api/v1/events/event-10" -> respond(eventJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = ActivityRepositoryImpl(
                client,
                observeSession = com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase(session),
            )
            val result = repository.getEventById("event-10")
            assertTrue(result is com.dev.lbsuhack.core.domain.DomainResult.Success)
            val proposal = (result as com.dev.lbsuhack.core.domain.DomainResult.Success).value
            assertTrue(proposal.isMine)
            assertTrue(proposal.hasVoted)
            assertEquals("event-10", proposal.id)
        }
    }

    @Test
    fun refreshKeepsCachedItemsWhenOneSourceFails() = runTest {
        var failVoting = false
        val votingEvent = """
            {
              "id": "v-1", "name": "Клуб", "description": null, "scope": "SCHOOL", "category": "OTHER",
              "status": "ON_VOTE", "declineReason": null, "voteThreshold": 10, "startsAt": null,
              "votingClosesAt": "2026-12-01T00:00:00.000Z", "successProbability": 0.5, "imageUrl": null,
              "likesCount": 0, "initiator": {"name": "А", "surname": "Б"}, "createdByRole": "PUPIL", "moderators": [],
              "voters": [], "votesCount": 0, "createdAt": "2026-11-01T00:00:00.000Z",
              "updatedAt": "2026-11-01T00:00:00.000Z", "hasVoted": false, "likedByMe": false
            }
        """.trimIndent()
        val myEvent = """
            {
              "id": "m-1", "name": "Моё событие", "description": null, "scope": "CLASS", "category": "SPORTS",
              "status": "ON_VOTE", "declineReason": null, "voteThreshold": 10, "startsAt": null,
              "votingClosesAt": "2026-12-01T00:00:00.000Z", "successProbability": 0.5, "imageUrl": null,
              "likesCount": 0, "initiator": {"name": "Я", "surname": "Автор"}, "createdByRole": "PUPIL", "moderators": [],
              "voters": [], "votesCount": 0, "createdAt": "2026-11-02T00:00:00.000Z",
              "updatedAt": "2026-11-02T00:00:00.000Z", "hasVoted": false, "likedByMe": false
            }
        """.trimIndent()
        val session = com.dev.lbsuhack.feature.activities.util.FakeSessionRepository(
            com.dev.lbsuhack.core.domain.session.SessionState.Authenticated(
                com.dev.lbsuhack.core.domain.session.SessionUser(
                    id = "me", name = "Я", surname = "Автор", role = com.dev.lbsuhack.core.domain.model.UserRole.PUPIL,
                ),
            ),
        )
        HttpClient(MockEngine { request ->
            val headers = headersOf(HttpHeaders.ContentType, "application/json")
            when (request.url.encodedPath) {
                "/api/v1/events/voting" -> {
                    if (failVoting) {
                        respond("Server error", HttpStatusCode.InternalServerError)
                    } else {
                        respond("[$votingEvent]", HttpStatusCode.OK, headers)
                    }
                }
                "/api/v1/events/my" -> respond("[$myEvent]", HttpStatusCode.OK, headers)
                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = ActivityRepositoryImpl(
                client,
                observeSession = com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase(session),
            )
            val firstRefresh = repository.refresh()
            assertTrue(firstRefresh is com.dev.lbsuhack.core.domain.DomainResult.Success)
            assertEquals(2, repository.observeActivities().first().size)

            failVoting = true
            val secondRefresh = repository.refresh()
            assertTrue(secondRefresh is com.dev.lbsuhack.core.domain.DomainResult.Failure)
            val activities = repository.observeActivities().first()
            assertEquals(2, activities.size)
            assertTrue(activities.any { it.id == "v-1" })
            assertTrue(activities.any { it.id == "m-1" })
        }
    }

    @Test
    fun concurrentVoteAndLikeExecuteSafely() = runTest {
        val event = """
            {
              "id": "e-sync", "name": "Синхронизация", "description": null, "scope": "CLASS", "category": "SPORTS",
              "status": "ON_VOTE", "declineReason": null, "voteThreshold": 10, "startsAt": null,
              "votingClosesAt": "2026-12-01T00:00:00.000Z", "successProbability": 0.5, "imageUrl": null,
              "likesCount": 1, "initiator": {"name": "А", "surname": "Б"}, "createdByRole": "PUPIL", "moderators": [],
              "voters": [], "votesCount": 0, "createdAt": "2026-11-01T00:00:00.000Z",
              "updatedAt": "2026-11-01T00:00:00.000Z", "hasVoted": false, "likedByMe": false
            }
        """.trimIndent()
        val session = com.dev.lbsuhack.feature.activities.util.FakeSessionRepository(
            com.dev.lbsuhack.core.domain.session.SessionState.Authenticated(
                com.dev.lbsuhack.core.domain.session.SessionUser(
                    id = "me", name = "Я", surname = "Ученик", role = com.dev.lbsuhack.core.domain.model.UserRole.PUPIL,
                ),
            ),
        )
        HttpClient(MockEngine { request ->
            val headers = headersOf(HttpHeaders.ContentType, "application/json")
            when (request.url.encodedPath) {
                "/api/v1/events/voting" -> respond("[$event]", HttpStatusCode.OK, headers)
                "/api/v1/events/my" -> respond("[]", HttpStatusCode.OK, headers)
                "/api/v1/events/e-sync/vote" -> respond(
                    """{"eventId": "e-sync", "votesCount": 1, "voteThreshold": 10, "status": "ON_VOTE"}""",
                    HttpStatusCode.OK,
                    headers,
                )
                "/api/v1/events/e-sync/like" -> respond(
                    """{"eventId": "e-sync", "liked": true, "likesCount": 2}""",
                    HttpStatusCode.OK,
                    headers,
                )
                else -> respond("Not found", HttpStatusCode.NotFound)
            }
        }) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }.use { client ->
            val repository = ActivityRepositoryImpl(
                client,
                observeSession = com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase(session),
                ioDispatcher = StandardTestDispatcher(testScheduler),
            )
            repository.refresh()
            val voteDeferred = async { repository.vote("e-sync") }
            val likeDeferred = async { repository.like("e-sync") }
            val voteRes = voteDeferred.await()
            val likeRes = likeDeferred.await()
            assertTrue(voteRes is com.dev.lbsuhack.core.domain.DomainResult.Success)
            assertTrue(likeRes is com.dev.lbsuhack.core.domain.DomainResult.Success)
            advanceUntilIdle()
            val finalItem = repository.observeActivities().first().first { it.id == "e-sync" }
            assertTrue(finalItem.hasVoted)
            assertTrue(finalItem.likedByMe)
            assertEquals(1, finalItem.votesCount)
            assertEquals(2, finalItem.likesCount)
        }
    }
}
