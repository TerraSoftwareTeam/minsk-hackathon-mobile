package com.dev.lbsuhack.feature.auth.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.user.UserRepository
import com.dev.lbsuhack.core.network.auth.AuthState
import com.dev.lbsuhack.core.network.auth.TokenStorage
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.client.AppHttpClientFactory
import com.dev.lbsuhack.core.network.client.AuthPlugin
import com.dev.lbsuhack.core.network.error.NetworkError
import com.dev.lbsuhack.feature.auth.domain.repository.SchoolsByAuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class BackendIntegrationTest {
    private val pupil = User.Pupil("school-id", "Иван", surname = "Иванов", pupilClass = "10А")
    private val schools = object : SchoolsByAuthRepository {
        override suspend fun loginAndGetUser(username: String, password: String, role: UserRole) = DomainResult.Success(pupil)
        override suspend fun loginAndGetPupil(username: String, password: String) = DomainResult.Success(pupil)
    }
    private val tokens = MemoryTokens()
    private val users = object : UserRepository {
        override val currentUser = MutableStateFlow<User?>(null)
        override suspend fun getCurrentUser(forceRefresh: Boolean): DomainResult<User> = DomainResult.Failure(DomainError.NotFound)
        override suspend fun updateCache(user: User) { currentUser.value = user }
        override suspend fun clearCache() { currentUser.value = null }
    }
    private val headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val authResponse = """{"accessToken":"server-token","user":{"id":"backend-id","schoolsById":"school-id","name":"Иван","surname":"Иванов","class":"10А","role":"PUPIL"}}"""

    @Test
    fun backendFailureNeverCreatesLocalSession() = runTest {
        HttpClient(MockEngine { respond("{}", HttpStatusCode.ServiceUnavailable, headers) }) {
            install(ContentNegotiation) { json(AppHttpClientFactory.defaultJson) }
        }.use { client ->
            val result = AuthRepositoryImpl(schools, client, tokens, users).login("user", "password", UserRole.PUPIL)
            assertEquals(DomainResult.Failure(DomainError.Network), result)
            assertNull(tokens.getAccessToken())
            assertNull(users.currentUser.value)
        }
    }

    @Test
    fun successfulRegistrationUsesBackendIdentityAndToken() = runTest {
        HttpClient(MockEngine { respond(authResponse, HttpStatusCode.OK, headers) }) {
            install(ContentNegotiation) { json(AppHttpClientFactory.defaultJson) }
        }.use { client ->
            val result = AuthRepositoryImpl(schools, client, tokens, users).login("user", "password", UserRole.PUPIL)
            assertEquals("backend-id", (result as DomainResult.Success).value.id)
            assertEquals("server-token", tokens.getAccessToken())
        }
    }

    @Test
    fun schoolsAuthenticationAlwaysCallsRegistrationOnly() = runTest {
        val paths = mutableListOf<String>()
        HttpClient(MockEngine {
            paths += it.url.encodedPath
            respond(authResponse, HttpStatusCode.Created, headers)
        }) {
            install(ContentNegotiation) { json(AppHttpClientFactory.defaultJson) }
        }.use { client ->
            val result = AuthRepositoryImpl(schools, client, tokens, users).login("user", "password", UserRole.PUPIL)
            assertTrue(result is DomainResult.Success)
            assertEquals(listOf("/api/v1/auth/register"), paths)
        }
    }

    @Test
    fun registrationSendsSchoolsProfileAndKeepsSchoolLocally() = runTest {
        val html = """
            <span data-id="school" class="name school-title"><a href="/">Гимназия &#8470; 41</a></span>
            <h1>Иванов Иван Петрович, <a href="/class/10">10A</a> класс, ID 3575208</h1>
        """.trimIndent()
        val schoolsRepository = com.dev.lbsuhack.feature.auth.data.repository.SchoolsByAuthRepositoryImpl(
            remoteDataSource = object : com.dev.lbsuhack.feature.auth.data.remote.SchoolsByRemoteDataSource {
                override suspend fun loginAndFetchProfileHtml(username: String, password: String) =
                    com.dev.lbsuhack.feature.auth.data.model.RemoteProfileResult.Success(html, "https://schools.by/pupil/3575208")
            },
            parser = com.dev.lbsuhack.feature.auth.data.parser.SchoolsByProfileParser(),
        )
        var registrationBody: kotlinx.serialization.json.JsonObject? = null
        val response = authResponse.replace("school-id", "3575208").replace("10А", "10A")
        HttpClient(MockEngine { request ->
            assertEquals("/api/v1/auth/register", request.url.encodedPath)
            registrationBody = AppHttpClientFactory.defaultJson.parseToJsonElement(
                (request.body as io.ktor.http.content.TextContent).text
            ) as kotlinx.serialization.json.JsonObject
            respond(response, HttpStatusCode.Created, headers)
        }) {
            install(ContentNegotiation) { json(AppHttpClientFactory.defaultJson) }
        }.use { client ->
            val result = AuthRepositoryImpl(schoolsRepository, client, tokens, users).login("user", "password", UserRole.PUPIL)
            val expected = """{"schoolsById":"3575208","name":"Иван","surname":"Иванов","class":"10А","role":"PUPIL"}"""
            assertEquals(AppHttpClientFactory.defaultJson.parseToJsonElement(expected), registrationBody)
            val session = (result as DomainResult.Success).value
            assertEquals("Гимназия № 41", session.schoolName)
            assertEquals("Гимназия № 41", users.currentUser.value?.schoolName)
            assertEquals("Иван", session.name)
            assertEquals("Иванов", session.surname)
            assertEquals("10A", session.pupilClass)
        }
    }

    @Test
    fun missingRegistrationEndpointDoesNotTriggerLogin() = runTest {
        var requests = 0
        HttpClient(MockEngine { requests++; respond("{}", HttpStatusCode.NotFound, headers) }) {
            install(ContentNegotiation) { json(AppHttpClientFactory.defaultJson) }
        }.use { client ->
            assertEquals(DomainResult.Failure(DomainError.NotFound), AuthRepositoryImpl(schools, client, tokens, users).login("user", "password", UserRole.PUPIL))
            assertEquals(1, requests)
        }
    }

    @Test
    fun registrationConflictDoesNotFallBackToLogin() = runTest {
        val paths = mutableListOf<String>()
        HttpClient(MockEngine {
            paths += it.url.encodedPath
            respond("""{"message":"User already exists"}""", HttpStatusCode.Conflict, headers)
        }) {
            install(ContentNegotiation) { json(AppHttpClientFactory.defaultJson) }
        }.use { client ->
            val result = AuthRepositoryImpl(schools, client, tokens, users).login("user", "password", UserRole.PUPIL)
            assertTrue(result is DomainResult.Failure)
            assertEquals(listOf("/api/v1/auth/register"), paths)
            assertNull(tokens.getAccessToken())
        }
    }

    @Test
    fun unauthorizedResponseInvalidatesTokenWithoutRetryingRequest() = runTest {
        tokens.saveTokens("expired", null)
        var invalidations = 0
        HttpClient(MockEngine {
            assertEquals("Bearer expired", it.headers[HttpHeaders.Authorization])
            respond("{}", HttpStatusCode.Unauthorized, headers)
        }) {
            expectSuccess = true
            install(AuthPlugin) {
                tokenStorage = tokens
                refreshTokens = { invalidations++; tokens.clear(); false }
            }
        }.use { client ->
            assertEquals(NetworkResult.Error(NetworkError.Unauthorized), safeCall { client.get("https://example.test/api/v1/events/my") })
            assertEquals(1, invalidations)
            assertNull(tokens.getAccessToken())
        }
    }

    @Test
    fun responseFromPreviousAccountDoesNotRetryAsNewUser() = runTest {
        tokens.saveTokens("previous-user", null)
        var requests = 0
        var invalidations = 0
        HttpClient(MockEngine {
            requests++
            tokens.saveTokens("new-user", null)
            respond("{}", HttpStatusCode.Unauthorized, headers)
        }) {
            expectSuccess = true
            install(AuthPlugin) {
                tokenStorage = tokens
                refreshTokens = { invalidations++; false }
            }
        }.use { client ->
            assertEquals(NetworkResult.Error(NetworkError.Unauthorized), safeCall { client.get("https://example.test/api/v1/events/my") })
            assertEquals(1, requests)
            assertEquals(0, invalidations)
            assertEquals("new-user", tokens.getAccessToken())
        }
    }

    @Test
    fun cancellationIsNotConvertedToNetworkFailure() = runTest {
        val cancelled = CancellationException("cancelled")
        try {
            safeCall<Unit> { throw cancelled }
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(cancelled, actual)
        }
    }

    @Test
    fun disconnectedNetworkIsNotReportedAsTimeout() = runTest {
        assertEquals(NetworkResult.Error(NetworkError.NoInternet), safeCall<Unit> { throw IOException("offline") })
    }

    private class MemoryTokens : TokenStorage {
        override val authState = MutableStateFlow(AuthState())
        override suspend fun getAccessToken() = authState.value.accessToken
        override suspend fun getRefreshToken() = authState.value.refreshToken
        override suspend fun saveTokens(accessToken: String, refreshToken: String?) { authState.value = AuthState(accessToken, refreshToken) }
        override suspend fun clear() { authState.value = AuthState() }
    }
}
