package com.dev.lbsuhack.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.context.GlobalContext

class KolaFirebaseMessagingService : FirebaseMessagingService() {

    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token")
        GlobalContext.getOrNull()?.getOrNull<PushTokenSync>()?.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]

        KolaNotificationManager.showNotification(
            context = this,
            title = title,
            message = body,
            data = remoteMessage.data,
        )
    }

    companion object {
        private const val TAG = "KolaFCMService"
    }
}
