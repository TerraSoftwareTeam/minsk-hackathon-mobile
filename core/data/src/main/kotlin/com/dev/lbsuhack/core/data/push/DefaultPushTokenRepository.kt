package com.dev.lbsuhack.core.data.push

import android.content.Context
import android.util.Log
import com.dev.lbsuhack.core.domain.push.PushTokenRepository
import com.dev.lbsuhack.core.network.auth.TokenStorage
import com.dev.lbsuhack.core.network.generated.model.DeleteDeviceTokenDto
import com.dev.lbsuhack.core.network.generated.model.RegisterDeviceTokenDto
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class DefaultPushTokenRepository(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private val deviceId: () -> String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PushTokenRepository {

    override suspend fun register(token: String): Boolean = send("register") { accessToken ->
        httpClient.post(PATH) {
            authorized(accessToken)
            setBody(
                RegisterDeviceTokenDto(
                    token = token,
                    platform = RegisterDeviceTokenDto.Platform.ANDROID,
                    deviceId = deviceId(),
                ),
            )
        }.status.isSuccess()
    }

    override suspend fun unregister(token: String): Boolean = send("unregister") { accessToken ->
        httpClient.delete(PATH) {
            authorized(accessToken)
            setBody(DeleteDeviceTokenDto(token = token))
        }.status.isSuccess()
    }

    private suspend fun send(action: String, block: suspend (String) -> Boolean): Boolean =
        withContext(ioDispatcher) {
            val accessToken = tokenStorage.getAccessToken()?.takeIf(String::isNotBlank)
                ?: return@withContext false
            try {
                block(accessToken).also { if (!it) Log.w(TAG, "Push token $action rejected") }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.w(TAG, "Push token $action failed: ${e.message}")
                false
            }
        }

    private fun HttpRequestBuilder.authorized(accessToken: String) {
        bearerAuth(accessToken)
        contentType(ContentType.Application.Json)
    }

    private companion object {
        const val TAG = "PushTokenRepository"
        const val PATH = "api/v1/device-token"
    }
}

class DeviceIdProvider(context: Context) : () -> String {

    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    override fun invoke(): String =
        preferences.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            preferences.edit().putString(KEY_DEVICE_ID, it).apply()
        }

    private companion object {
        const val PREFS_NAME = "push_device"
        const val KEY_DEVICE_ID = "device_id"
    }
}
