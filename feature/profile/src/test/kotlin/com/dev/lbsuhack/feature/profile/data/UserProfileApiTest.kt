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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileApiTest {

    private val json = AppHttpClientFactory.defaultJson

    @Test
    fun updateProfileSendsPatchAndReturnsPupil() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        var capturedPath: String? = null
        var capturedMethod: HttpMethod? = null
        var capturedBody: String? = null

        val mockEngine = MockEngine { request ->
            capturedPath = request.url.encodedPath
            capturedMethod = request.method
            capturedBody = when (val body = request.body) {
                is OutgoingContent.ByteArrayContent -> body.bytes().decodeToString()
                else -> null
            }

            respond(
                content = """
                    {
                      "id": "user-42",
                      "schoolsById": "school-99",
                      "name": "Ivan",
                      "surname": "Ivanov",
                      "class": "11-B",
                      "role": "PUPIL"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val api = UserProfileApi(client, testDispatcher)
        val result = api.updateProfile(userId = "user-42", name = " Ivan ", surname = " Ivanov ")

        assertEquals("/api/v1/users/user-42", capturedPath)
        assertEquals(HttpMethod.Patch, capturedMethod)
        assertTrue(capturedBody?.contains("\"name\":\"Ivan\"") == true)
        assertTrue(capturedBody?.contains("\"surname\":\"Ivanov\"") == true)

        assertTrue(result is DomainResult.Success)
        val user = (result as DomainResult.Success).value
        assertTrue(user is User.Pupil)
        val pupil = user as User.Pupil
        assertEquals("user-42", pupil.id)
        assertEquals("Ivan", pupil.name)
        assertEquals("Ivanov", pupil.surname)
        assertEquals("school-99", pupil.schoolsById)
        assertEquals("11-B", pupil.pupilClass)
    }

    @Test
    fun updateProfileMapsTeacherAndAdmin() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val mockEngine = MockEngine { request ->
            val isTeacher = request.url.encodedPath.contains("teacher")
            val role = if (isTeacher) "TEACHER" else "ADMIN"
            val id = if (isTeacher) "teacher-1" else "admin-1"
            respond(
                content = """
                    {
                      "id": "$id",
                      "schoolsById": "school-1",
                      "name": "Anna",
                      "surname": "Petrova",
                      "role": "$role"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val api = UserProfileApi(client, testDispatcher)

        val teacherResult = api.updateProfile("teacher-1", "Anna", "Petrova")
        assertTrue(teacherResult is DomainResult.Success)
        assertTrue((teacherResult as DomainResult.Success).value is User.Teacher)

        val adminResult = api.updateProfile("admin-1", "Anna", "Petrova")
        assertTrue(adminResult is DomainResult.Success)
        assertTrue((adminResult as DomainResult.Success).value is User.Organizator)
    }

    @Test
    fun updateProfileReturnsFailureOnError() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val mockEngine = MockEngine {
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val api = UserProfileApi(client, testDispatcher)
        val result = api.updateProfile("user-1", "A", "B")

        assertTrue(result is DomainResult.Failure)
    }
}
