package com.dev.lbsuhack.feature.profile.data

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.network.client.AppHttpClientFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileRepositoryImplTest {

    private class FakeProfilePreferences : ProfilePreferences {
        private var notifications = true

        override fun isNotificationsEnabled(): Boolean = notifications

        override fun setNotificationsEnabled(enabled: Boolean) {
            notifications = enabled
        }
    }

    @Test
    fun notificationsFlagCanBeToggled() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val mockEngine = MockEngine { respond("", HttpStatusCode.OK) }
        val client = HttpClient(mockEngine)
        val api = UserProfileApi(client, testDispatcher)
        val prefs = FakeProfilePreferences()
        val repository = ProfileRepositoryImpl(prefs, api)

        assertTrue(repository.isNotificationsEnabled())
        repository.setNotificationsEnabled(false)
        assertFalse(repository.isNotificationsEnabled())
        repository.setNotificationsEnabled(true)
        assertTrue(repository.isNotificationsEnabled())
    }

    @Test
    fun savedCountReturnsZero() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val mockEngine = MockEngine { respond("", HttpStatusCode.OK) }
        val client = HttpClient(mockEngine)
        val api = UserProfileApi(client, testDispatcher)
        val repository = ProfileRepositoryImpl(FakeProfilePreferences(), api)

        assertEquals(0, repository.savedCount())
    }

    @Test
    fun updateOwnProfileDelegatesToApi() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val mockEngine = MockEngine {
            respond(
                content = """
                    {
                      "id": "user-1",
                      "schoolsById": "school-1",
                      "name": "Maria",
                      "surname": "Sidorova",
                      "class": "9-A",
                      "role": "PUPIL"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(AppHttpClientFactory.defaultJson)
            }
        }
        val api = UserProfileApi(client, testDispatcher)
        val repository = ProfileRepositoryImpl(FakeProfilePreferences(), api)

        val result = repository.updateOwnProfile("user-1", "Maria", "Sidorova")
        assertTrue(result is DomainResult.Success)
        val user = (result as DomainResult.Success).value
        assertEquals("Maria", user.name)
        assertEquals("Sidorova", user.surname)
    }
}
