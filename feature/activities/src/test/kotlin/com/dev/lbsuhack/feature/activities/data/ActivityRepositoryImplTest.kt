package com.dev.lbsuhack.feature.activities.data

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
import kotlinx.coroutines.test.runTest
import com.dev.lbsuhack.core.network.client.AppHttpClientFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityRepositoryImplTest {

    private val json = AppHttpClientFactory.defaultJson

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
                                "teacher": {
                                  "id": "2",
                                  "schoolsById": "school-2",
                                  "name": "Петр",
                                  "surname": "Петров",
                                  "class": "10A",
                                  "role": "TEACHER"
                                },
                                "createdAt": "2026-11-06T00:00:00.000Z",
                                "updatedAt": "2026-11-06T00:00:00.000Z",
                                "votesCount": 5,
                                "hasVoted": false
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
        assertEquals("school-1", event.value.authorSchoolsById)
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
                                "teacher": {
                                  "id": "2",
                                  "schoolsById": "school-2",
                                  "name": "Петр",
                                  "surname": "Петров",
                                  "class": "10A",
                                  "role": "TEACHER"
                                },
                                "createdAt": "2026-11-06T00:00:00.000Z",
                                "updatedAt": "2026-11-06T00:00:00.000Z",
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
                          "teacher": {
                            "id": "2",
                            "schoolsById": "school-2",
                            "name": "Петр",
                            "surname": "Петров",
                            "class": "10A",
                            "role": "TEACHER"
                          },
                          "createdAt": "2026-11-06T00:00:00.000Z",
                          "updatedAt": "2026-11-06T00:00:00.000Z"
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
            startsAt = "2026-11-21T10:00:00.000Z",
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
                                "teacher": {
                                  "id": "2",
                                  "schoolsById": "school-2",
                                  "name": "Петр",
                                  "surname": "Петров",
                                  "class": "10A",
                                  "role": "TEACHER"
                                },
                                "createdAt": "2026-11-06T00:00:00.000Z",
                                "updatedAt": "2026-11-06T00:00:00.000Z",
                                "votesCount": 5,
                                "hasVoted": false
                              }"""
        HttpClient(MockEngine { request ->
            assertEquals(io.ktor.http.HttpMethod.Put, request.method)
            assertEquals("/api/v1/events/vote-1/image", request.url.encodedPath)
            assertTrue(request.body is io.ktor.client.request.forms.MultiPartFormDataContent)
            respond(fixture, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
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
    fun mainFeedUsesRoleAppropriateEndpoint() = runTest {
        val roles = mapOf(
            com.dev.lbsuhack.core.domain.model.UserRole.PUPIL to "/api/v1/events/voting",
            com.dev.lbsuhack.core.domain.model.UserRole.TEACHER to "/api/v1/events/voting",
            com.dev.lbsuhack.core.domain.model.UserRole.ORGANIZATOR to "/api/v1/events/admin-moderation",
        )
        for ((role, path) in roles) {
            val requests = mutableListOf<String>()
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
                assertEquals(listOf(path), requests)
            }
        }
    }

}
