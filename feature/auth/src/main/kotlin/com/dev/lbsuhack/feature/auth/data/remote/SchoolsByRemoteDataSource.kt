package com.dev.lbsuhack.feature.auth.data.remote

import com.dev.lbsuhack.feature.auth.data.model.RemoteProfileResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface SchoolsByRemoteDataSource {
    suspend fun loginAndFetchProfileHtml(username: String, password: String): RemoteProfileResult
}

class SchoolsByRemoteDataSourceImpl(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val baseUrl: String = "https://schools.by",
) : SchoolsByRemoteDataSource {

    private val userAgent =
        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    private val csrfTokenRegex = Regex(
        pattern = """name=["']csrfmiddlewaretoken["']\s+value=["']([^"']+)["']|value=["']([^"']+)["']\s+name=["']csrfmiddlewaretoken["']""",
        options = setOf(RegexOption.IGNORE_CASE),
    )

    private val csrfCookieRegex = Regex("""csrftoken=([^;]+)""")
    private val sessionIdCookieRegex = Regex("""sessionid=([^;]+)""")

    override suspend fun loginAndFetchProfileHtml(
        username: String,
        password: String,
    ): RemoteProfileResult = withContext(ioDispatcher) {
        try {
            val loginPageUrl = "$baseUrl/login"
            val getConn = (URL(loginPageUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Referer", "$baseUrl/")
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            val getHtml = readResponse(getConn)
            val cookiesAfterGet = getConn.headerFields["Set-Cookie"].orEmpty()
            getConn.disconnect()

            val csrfCookie = extractCookie(cookiesAfterGet, csrfCookieRegex)
            val csrfToken = extractCsrfToken(getHtml)

            if (csrfCookie == null || csrfToken == null) {
                return@withContext RemoteProfileResult.Error(
                    IllegalStateException("Could not obtain CSRF token or cookie from Schools.by login page")
                )
            }

            val postConn = (URL(loginPageUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                instanceFollowRedirects = false
                doOutput = true
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Referer", loginPageUrl)
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("Cookie", "csrftoken=$csrfCookie")
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            val body = buildString {
                append("username=").append(URLEncoder.encode(username, StandardCharsets.UTF_8.name()))
                append("&password=").append(URLEncoder.encode(password, StandardCharsets.UTF_8.name()))
                append("&csrfmiddlewaretoken=").append(URLEncoder.encode(csrfToken, StandardCharsets.UTF_8.name()))
                append("&%7C123=%7C123")
            }

            OutputStreamWriter(postConn.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(body)
                writer.flush()
            }

            val postStatus = postConn.responseCode
            val cookiesAfterPost = postConn.headerFields["Set-Cookie"].orEmpty()
            val locationHeader = postConn.getHeaderField("Location")
            postConn.disconnect()

            if (postStatus == HttpURLConnection.HTTP_OK) {
                return@withContext RemoteProfileResult.InvalidCredentials
            }

            if (postStatus !in listOf(
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_SEE_OTHER,
                    307,
                    308
                ) || locationHeader.isNullOrBlank()
            ) {
                return@withContext RemoteProfileResult.InvalidCredentials
            }

            if (locationHeader.contains("/login")) {
                return@withContext RemoteProfileResult.InvalidCredentials
            }

            val sessionIdCookie = extractCookie(cookiesAfterPost, sessionIdCookieRegex)
            val fullProfileUrl = if (locationHeader.startsWith("http://") || locationHeader.startsWith("https://")) {
                locationHeader
            } else {
                "$baseUrl${if (locationHeader.startsWith("/")) "" else "/"}$locationHeader"
            }

            val profileConn = (URL(fullProfileUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Referer", loginPageUrl)
                val cookieHeader = buildString {
                    append("csrftoken=").append(csrfCookie)
                    if (!sessionIdCookie.isNullOrBlank()) {
                        append("; sessionid=").append(sessionIdCookie)
                    }
                }
                setRequestProperty("Cookie", cookieHeader)
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            val profileStatus = profileConn.responseCode
            if (profileStatus != HttpURLConnection.HTTP_OK) {
                profileConn.disconnect()
                return@withContext RemoteProfileResult.Error(
                    java.io.IOException("Profile request failed with HTTP $profileStatus")
                )
            }

            val profileHtml = readResponse(profileConn)
            val finalUrl = profileConn.url.toString()
            profileConn.disconnect()

            RemoteProfileResult.Success(html = profileHtml, profileUrl = finalUrl)
        } catch (e: Exception) {
            RemoteProfileResult.Error(e)
        }
    }

    private fun extractCsrfToken(html: String): String? {
        val match = csrfTokenRegex.find(html) ?: return null
        return match.groupValues[1].takeIf { it.isNotBlank() }
            ?: match.groupValues[2].takeIf { it.isNotBlank() }
    }

    private fun extractCookie(setCookieHeaders: List<String>, regex: Regex): String? {
        for (header in setCookieHeaders) {
            val match = regex.find(header)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val stream: InputStream = if (conn.responseCode in 200..399) {
            conn.inputStream
        } else {
            conn.errorStream ?: conn.inputStream
        }
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }
}
